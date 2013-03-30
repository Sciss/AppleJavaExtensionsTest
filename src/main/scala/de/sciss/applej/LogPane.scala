package de.sciss.applej

import javax.swing._
import java.io.{OutputStream, Writer}
import util.control.NonFatal

class LogPane extends JTextArea(20, 40) {
  pane =>

  private var totalLength = 0

  setEditable(false)
  setLineWrap(true)

  override def append(str: String) {
    super.append(str)
    totalLength += str.length
    updateCaret()
  }

  override def setText(str: String) {
    super.setText(str)
    totalLength = if (str == null) 0 else str.length
  }

  private def updateCaret() {
    try {
      setCaretPosition(math.max(0, totalLength - 1))
    }
    catch {
      case NonFatal(_) => /* ignore */
    }
  }

  // ---- Writer ----
  val writer: Writer = new Writer {
    override def toString = pane.toString + ".writer"

    def close() {}
    def flush() {}

    def write(ch: Array[Char], off: Int, len: Int) {
      val str = new String(ch, off, len)
      pane.append(str)
    }
  }

  // ---- OutputStream ----
  val outputStream: OutputStream = new OutputStream {
    override def toString = pane.toString + ".outputStream"

    override def write(b: Array[Byte], off: Int, len: Int) {
      val str = new String(b, off, len)
      pane.append(str)
    }

    def write(b: Int) {
      write(Array(b.toByte), 0, 1)
    }
  }

  private def makeDefault() {
    Console.setOut(outputStream)
    Console.setErr(outputStream)
    System.setOut(Console.out)
    System.setErr(Console.err)
  }

  makeDefault()
}