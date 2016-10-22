package libertalia
package module

import data._

import cats.Show
import cats.syntax.show._

object Message extends CrudModule[Model.Message, Datastore.msgs.type] with PossessiveModule[Model.Message, Datastore.msgs.type]  with ShowMessage {
  override val name   = "msg"
  override val source = Datastore.msgs
  val editor          = new util.FileEditor(config.editorPath)

  val messageProcessor: ProcessCmd = {
    case Cmd.create :: from  :: to :: title :: Nil  => create           { Model.Message(name, editor.create(), from.toInt, to.toInt) }
    case Cmd.update :: id          :: title :: Nil  => update(id.toInt) { d => d.copy(title = title, text = editor.edit(d.text))     }
    case Cmd.open   :: id                   :: Nil  => update(id.toInt) { d => d.copy(               text = editor.edit(d.text))     }
  }

  override val processor = messageProcessor orElse possessiveProcessor orElse crudProcessor
}

trait ShowMessage extends ShowEntity[Model.Message] {
  val snippetSize = 100

  override implicit val showEntity: Show[Model.Message] = Show.show { msg =>
    import msg._
    s"""${id.get} $title
       |From: $sender
       |To  : $recipient
       |Excerpt:
       |${text.take(snippetSize)}""".stripMargin    
  }

  override implicit val showList: Show[List[Model.Message]] = Show.show(_.map(_.show).mkString("\n======\n"))
}
