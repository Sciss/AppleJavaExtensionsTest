package de.sciss.applej

import swing._
import com.apple.eawt._
import com.apple.eawt.AppEvent._
import java.text.SimpleDateFormat
import java.util.{Date, Locale}
import Swing._
import com.apple.eawt.event._
import java.awt
import awt.image.BufferedImage
import awt._
import geom.{Ellipse2D, Area}
import javax.swing.{JScrollPane, Timer}
import swing.Button
import swing.Menu
import swing.MenuBar
import swing.Frame
import swing.Color
import swing.MenuItem

object App extends SimpleSwingApplication {
  sys.props("apple.laf.useScreenMenuBar") = "true"
  sys.props("com.apple.mrj.application.apple.menu.about.name") = "EAWT Test"

  val app = Application.getApplication

  def delayed(block: => Unit) {
    var cnt = 4
    lazy val t: Timer = new Timer(1000, ActionListener { _ =>
      cnt -= 1
      app.setDockIconBadge(cnt.toString)
      info(s"counting down $cnt")
      if (cnt == 0) {
        t.stop()
        block
      }
    })
    t.setInitialDelay(0)
    t.start()
  }

  lazy val top = new Frame {
    f =>

    title = "Apple Java Extensions Test"
    contents = new BorderPanel {
      add(new Button("<html><body>Listening for<p>ApplicationEvents and<p>GestureEvents</body></html>") {
        peer.setPreferredSize((400, 400))
        GestureUtilities.addGestureListenerTo(peer, GestureListener)
      }, BorderPanel.Position.Center)

      add(new GridPanel(2, 2) {
        contents += Button("requestForeground(false)") {
          delayed(app.requestForeground(false))
        }
        contents += Button("requestUserAttention(false)") {
          delayed(app.requestUserAttention(false))
        }
        contents += Button("requestForeground(true)") {
          delayed(app.requestForeground(true))
        }
        contents += Button("requestUserAttention(true)") {
          delayed(app.requestUserAttention(true))
        }
      }, BorderPanel.Position.South)
    }
    app.addAppEventListener(AppListener)
    app.setAboutHandler(About)
    app.setQuitHandler(Quit)
    app.setPreferencesHandler(Preferences)  // creates the corresponding item in the apple menu!
//    app.setPrintFileHandler(Print)
    app.setOpenFileHandler(Open)

    pack()
    centerOnScreen()

    val aux = new Frame {
      title = "Log"
      contents = swing.Component.wrap(new JScrollPane(new LogPane))
      pack()
      location = { val p = f.location; p.x += f.size.width + 32; p }
      open()
    }

    val mb  = new MenuBar {
      contents += new Menu("Foo") {
        contents += new MenuItem(Action("Show Window") {
          f.open()
          aux.open()
        })
      }
    }
    menuBar = mb
    app.setDefaultMenuBar(mb.peer)

    // setting dock menu doesn't seem to work...
    val pop = new awt.PopupMenu("Apples") {
      add(new awt.MenuItem("Bananas") {
        addActionListener(ActionListener( _ => info("dock menu action")))
      })
    }
//    app.setDockMenu(pop)

    app.setDockIconImage(DockImage)
    app.setDockIconBadge("666")

    // ---- this is not apple provided but part of the java se ----
    if (SystemTray.isSupported) {
      val tray  = SystemTray.getSystemTray
      val icn   = new TrayIcon(TrayImage(tray.getTrayIconSize), "Foo Bar", pop)
      tray.add(icn)

    } else {
      info("SystemTray not supported")
    }
  }

  private val logHeader = new SimpleDateFormat("[d MMM yyyy, HH:mm''ss.SSS]", Locale.US)

  def info(what: String) {
    println(s"${logHeader.format(new Date())} $what")
  }

  object About extends AboutHandler {
    def handleAbout(e: AboutEvent) {
      info("handleAbout")
    }
  }

  object Quit extends QuitHandler {
    def handleQuitRequestWith(e: QuitEvent, r: QuitResponse) {
      info("handleQuitRequestWith")
      r.performQuit()
    }
  }

  object Preferences extends PreferencesHandler {
    def handlePreferences(e: PreferencesEvent) {
      info("handlePreferences")
    }
  }

//  // this doesn't seem to have any effect
//  object Print extends PrintFilesHandler {
//    def printFiles(e: PrintFilesEvent) {
//      info("printFiles")
//    }
//  }

  object Open extends OpenFilesHandler {
    def openFiles(e: OpenFilesEvent) {
      import collection.JavaConversions._
      val f = e.getFiles.map(f => s"'$f'").mkString(", ")
      val t = e.getSearchTerm
      // note: the search term always seems to be null, even when using spotlight
      info(s"open files $f, search term '$t'")
    }
  }

  val DockImage = {
    val i = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB)
    val g = i.createGraphics()
    g.setPaint(new LinearGradientPaint(0f, 0f, 255f, 255f,
      Array(0f, 0.5f, 1f), Array(Color.red, Color.blue, new Color(0x00, 0x00, 0xFF, 0x00))))
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.fillRoundRect(0, 0, 256, 256, 32, 32)
    g.dispose()
    i
  }

  def TrayImage(sz: Dimension) = {
    val w = sz.width - 4
    val h = sz.height - 4
    val i = new BufferedImage(sz.width, sz.height, BufferedImage.TYPE_INT_ARGB)
    val g = i.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    val r1 = new Ellipse2D.Float(0, 0, w, h)
    val r2 = new Ellipse2D.Float(w * 0.25f, h * 0.25f, w * 0.5f, h * 0.5f)
    val a  = new Area(r1)
    a.subtract(new Area(r2))
    g.setColor(Color.white)
    g.translate(2, 3)
    g.fill(a)
    g.translate(0, -1)
    g.setColor(Color.black)
    g.fill(a)
    g.dispose()
    i
  }

  object GestureListener extends GesturePhaseListener with MagnificationListener 
  with RotationListener with SwipeListener {
    def gestureBegan(e: GesturePhaseEvent) {
      info("gestureBegan")
    }

    def gestureEnded(e: GesturePhaseEvent) {
      info("gestureEnded")
    }

    def rotate(e: RotationEvent) {
      info(f"rotate ${e.getRotation}%1.3f")
    }

    def magnify(e: MagnificationEvent) {
      info(f"magnify ${e.getMagnification}%1.3f")
    }

    def swipedUp(e: SwipeEvent) {
      info("swipedUp")
    }

    def swipedDown(e: SwipeEvent) {
      info("swipedDown")
    }

    def swipedLeft(e: SwipeEvent) {
      info("swipedLeft")
    }

    def swipedRight(e: SwipeEvent) {
      info("swipedRight")
    }
  }

  object AppListener extends AppReOpenedListener with AppForegroundListener with AppHiddenListener
  with ScreenSleepListener with SystemSleepListener with UserSessionListener {
    def appReOpened(e: AppReOpenedEvent) {
      info("appReOpened")
      app.setDockIconBadge(null)
    }

    def appRaisedToForeground(e: AppForegroundEvent) {
      info("appRaisedToForeground")
    }

    def appMovedToBackground(e: AppForegroundEvent) {
      info("appMovedToBackground")
    }

    def appHidden(e: AppHiddenEvent) {
      info("appHidden")
    }

    def appUnhidden(e: AppHiddenEvent) {
      info("appUnhidden")
    }

    def screenAboutToSleep(e: ScreenSleepEvent) {
      info("screenAboutToSleep")
    }

    def screenAwoke(e: ScreenSleepEvent) {
      info("screenAwoke")
    }

    def systemAboutToSleep(e: SystemSleepEvent) {
      info("systemAboutToSleep")
    }

    def systemAwoke(e: SystemSleepEvent) {
      info("systemAwoke")
    }

    def userSessionDeactivated(e: UserSessionEvent) {
      info("userSessionDeactivated")
    }

    def userSessionActivated(e: UserSessionEvent) {
      info("userSessionActivated")
    }
  }
}