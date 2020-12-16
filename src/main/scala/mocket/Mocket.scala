package mocket

import indigo._
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.Random

@JSExportTopLevel("IndigoGame")
object Mocket extends IndigoSandbox[Unit, Model] {

  val config: GameConfig = GameConfig(
    viewport = GameViewport(3840, 2160),
    frameRate = 60,
    clearColor = ClearColor.Black,
    magnification = 1
  )

  val animations: Set[Animation] =
    Set()

  val assetName = AssetName("rocket")

  val assets: Set[AssetType] =
    Set(
      AssetType.Image(AssetName("rocket"), AssetPath("assets/rocket.png"))
    )

  val fonts: Set[FontInfo] =
    Set()

  def setup(
      assetCollection: AssetCollection,
      dice: Dice
  ): Startup[Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Model = Model.initial

  def updateModel(
      context: FrameContext[Unit],
      model: Model
  ): GlobalEvent => Outcome[Model] = {
    case MouseEvent.Click(x, _) =>
      Outcome(model.addRocket(Rocket.initial(
        Point(x, config.viewport.height),
        Random.between(0, config.viewport.height / 2),
        Radians(Random.between(-0.02, 0.02))
      )))

    case FrameTick =>
      Outcome(model.update(context.delta))

    case _ =>
      Outcome(model)
  }

  def present(
      context: FrameContext[Unit],
      model: Model
  ): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(
      drawRocket(model.rockets)
    )

  def drawRocket(
      rockets: List[Rocket]
  ): List[Graphic] =
    rockets.map(rocket =>
      Graphic(Rectangle(0, 0, 512, 1204), 1, Material.Textured(assetName))
        .withRef(256, 1204)
        .scaleBy(0.25, 0.25)
        .rotate(rocket.angle)
        .moveTo(rocket.location)
    )


}

case class Model(rockets: List[Rocket]) {
  def addRocket(rocket: Rocket): Model =
    this.copy(rockets =  (rocket :: rockets).take(300))

  def update(timeDelta: Seconds): Model =
    this.copy(rockets = rockets.map(_.update(timeDelta)))
}
object Model {
  def initial: Model = Model(List.empty)
}
case class Rocket(location: Point, angle: Radians, rotateBy: Radians, explodeAt: Int, state: RocketState) {
  def update(timeDelta: Seconds): Rocket = state match {
    case Waiting => this
    case Flying if location.y < explodeAt => this.copy(state = Boom(0))
    case Flying =>
      val newAngle = angle + rotateBy
      val distance = (60.0*35*timeDelta.value)
      // 0 is 90 degrees off rocket heading -> switch sin/cos
      val newX = location.x - (distance*Math.sin(newAngle.value))
      val newY = location.y - (distance*Math.cos(newAngle.value))
      this.copy(location = Point(newX.toInt, newY.toInt ), angle = newAngle )
    case Boom(_) => this.copy(location = Point(10000, 10000), state = Waiting) // TODO explodey steps
  }
}
object Rocket {
  def initial(location: Point, explodeAt: Int, rotateBy: Radians): Rocket = Rocket(location, Radians(0.0), rotateBy, explodeAt, Flying)
}

sealed trait RocketState
case object Waiting extends RocketState
case object Flying extends RocketState
case class Boom(frame: Int) extends RocketState