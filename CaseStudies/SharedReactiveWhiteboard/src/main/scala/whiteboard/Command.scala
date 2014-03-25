package whiteboard

import whiteboard.figures.Shape

trait Command
case object ClearCommand extends Command
case class ShapeCommand(shape: Shape) extends Command
