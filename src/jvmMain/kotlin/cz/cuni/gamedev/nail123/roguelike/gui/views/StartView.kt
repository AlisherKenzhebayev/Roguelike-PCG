package cz.cuni.gamedev.nail123.roguelike.gui.views

import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.ComponentDecorations
import org.hexworks.zircon.api.Components
import org.hexworks.zircon.api.component.ComponentAlignment
import org.hexworks.zircon.api.graphics.BoxType
import org.hexworks.zircon.api.grid.TileGrid
import org.hexworks.zircon.api.uievent.ComponentEventType
import org.hexworks.zircon.api.uievent.UIEventResponse
import org.hexworks.zircon.api.view.base.BaseView

class StartView(val tileGrid: TileGrid): BaseView(tileGrid, ColorThemes.arc()) {
    override fun onDock() {
        val msg = "Welcome to:"
        val msg2 = "    \"Rats, Orcs, Dungeon and some limping chests!!!\""
        val header = Components.textBox(msg2.length)
                .addHeader(msg)
                .addNewLine()
                .addHeader(msg2)
                .withAlignmentWithin(screen, ComponentAlignment.CENTER)
                .build()

        val startButton = Components.button()
                // we align the button to the bottom center of our header
                .withAlignmentAround(header, ComponentAlignment.BOTTOM_CENTER)
                .withText("Start!") // its text is "Start!"
                .withDecorations(
                        ComponentDecorations.box(BoxType.SINGLE),
                        ComponentDecorations.shadow()
                )
                .build()

        startButton.handleComponentEvents(ComponentEventType.ACTIVATED) {
            // Goto play_view
            replaceWith(PlayView(tileGrid))
            UIEventResponse.processed()
        }

        screen.addComponent(header)
        screen.addComponent(startButton)
    }
}