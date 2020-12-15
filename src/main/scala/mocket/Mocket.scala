package mocket

import indigo._
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object Mocket extends IndigoSandbox[Unit, Model] {

  val magnification = 3

  val config: GameConfig =
    GameConfig.default.withMagnification(magnification)

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

  def initialModel(startupData: Unit): Model =
    Model.initial(
      config.viewport.giveDimensions(magnification).center
    )

  def updateModel(
      context: FrameContext[Unit],
      model: Model
  ): GlobalEvent => Outcome[Model] = {
    case MouseEvent.Click(x, y) =>
      Outcome(model.addRocket(Rocket.initial(Point(x, y))))

    case FrameTick =>
      Outcome(model.update(context.delta))

    case _ =>
      Outcome(model)
  }

  def present(
      context: FrameContext[Unit],
      model: Model
  ): SceneUpdateFragment =
    SceneUpdateFragment(
      Graphic(Rectangle(0, 0, 32, 32), 1, Material.Textured(assetName))
    ).addGameLayerNodes(
      drawRocket(model.center, model.rocket)
    )

  def drawRocket(
      center: Point,
      dots: Rocket
  ): List[Graphic] =
    List(
      Graphic(Rectangle(0, 0, 32, 32), 1, Material.Textured(assetName))
        .withRef(8, 8)
        .moveTo(center)
    )

}

case class Model(center: Point, rocket: Rocket) {
  def addRocket(rocket: Rocket): Model =
    this.copy(rocket = rocket)

  def update(timeDelta: Seconds): Model =
    this.copy(rocket = rocket.update(timeDelta))
}
object Model {
  def initial(center: Point): Model = Model(center, Rocket.initial(center))
}
case class Rocket(Location: Point, angle: Radians, state: RocketState) {
  def update(timeDelta: Seconds): Rocket =
    this.copy(angle = angle + Radians.fromSeconds(timeDelta))
}
object Rocket {
  def initial(location: Point): Rocket = Rocket(location, Radians(0.0), Waiting)
}

sealed trait RocketState
case object Waiting extends RocketState
case object Flying extends RocketState
case class Boom(val frame: Int) extends RocketState