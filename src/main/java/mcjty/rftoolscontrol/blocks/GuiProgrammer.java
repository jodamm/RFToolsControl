package mcjty.rftoolscontrol.blocks;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.WindowManager;
import mcjty.lib.gui.events.IconEvent;
import mcjty.lib.gui.icons.IIcon;
import mcjty.lib.gui.icons.IconManager;
import mcjty.lib.gui.icons.ImageIcon;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.logic.GridInstance;
import mcjty.rftoolscontrol.logic.ProgramCardInstance;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GuiProgrammer extends GenericGuiContainer<ProgrammerTileEntity> {
    public static final int SIDEWIDTH = 80;
    public static final int WIDTH = 256;
    public static final int HEIGHT = 236;

    public static final int GRID_HEIGHT = 10;
    public static final int GRID_WIDTH = 11;

    public static int ICONSIZE = 20;

    private static final ResourceLocation mainBackground = new ResourceLocation(RFToolsControl.MODID, "textures/gui/testgui.png");
    private static final ResourceLocation sideBackground = new ResourceLocation(RFToolsControl.MODID, "textures/gui/sidegui.png");
    private static final ResourceLocation icons = new ResourceLocation(RFToolsControl.MODID, "textures/gui/icons.png");

    private Window sideWindow;
    private IconManager iconManager;
    private WidgetList gridList;

    private static final Map<String, IIcon> ICONS = new HashMap<>();

    static {
        ICONS.put("1", new ImageIcon(String.valueOf("1")).setDimensions(ICONSIZE, ICONSIZE).setImage(icons, 0*ICONSIZE, 0*ICONSIZE));
        ICONS.put("2", new ImageIcon(String.valueOf("2")).setDimensions(ICONSIZE, ICONSIZE).setImage(icons, 1*ICONSIZE, 0*ICONSIZE));
        ICONS.put("3", new ImageIcon(String.valueOf("3")).setDimensions(ICONSIZE, ICONSIZE).setImage(icons, 2*ICONSIZE, 0*ICONSIZE));
    }

    public GuiProgrammer(ProgrammerTileEntity tileEntity, ProgrammerContainer container) {
        super(RFToolsControl.instance, RFToolsCtrlMessages.INSTANCE, tileEntity, container, RFToolsControl.GUI_MANUAL_CONTROL, "programmer");

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        // --- Main window ---
        Panel editorPanel = setupEditorPanel();
        Panel controlPanel = setupControlPanel();
        Panel gridPanel = setupGridPanel();
        Panel toplevel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(mainBackground)
                .addChild(editorPanel)
                .addChild(controlPanel)
                .addChild(gridPanel);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));
        window = new Window(this, toplevel);

        // --- Side window ---
        Panel listPanel = setupListPanel();
        Panel sidePanel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(sideBackground)
                .addChild(listPanel);
        sidePanel.setBounds(new Rectangle(guiLeft-SIDEWIDTH, guiTop, SIDEWIDTH, ySize));
        sideWindow = new Window(this, sidePanel);

        readProgramCard();
    }

    @Override
    protected void registerWindows(WindowManager mgr) {
        super.registerWindows(mgr);
        mgr.addWindow(sideWindow);
        mgr.getIconManager().setClickHoldToDrag(true);
    }

    private Panel setupGridPanel() {

        Panel panel = new Panel(mc, this).setLayout(new PositionalLayout())
                .setLayoutHint(new PositionalLayout.PositionalHint(5, 5, 246, 113));

        gridList = new WidgetList(mc, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(0, 0, 236, 113))
                .setPropagateEventsToChildren(true)
                .setInvisibleSelection(true)
                .setDrawHorizontalLines(false)
                .setRowheight(ICONSIZE+1);
        Slider slider = new Slider(mc, this)
                .setVertical()
                .setScrollable(gridList)
                .setLayoutHint(new PositionalLayout.PositionalHint(237, 0, 9, 113));

        for (int y = 0; y < GRID_HEIGHT; y++) {
            Panel rowPanel = new Panel(mc, this).setLayout(new HorizontalLayout().setSpacing(-1).setHorizontalMargin(0).setVerticalMargin(0));
            for (int x = 0; x < GRID_WIDTH; x++) {
                IconHolder holder = new IconHolder(mc, this)
                        .setDesiredWidth(ICONSIZE+2)
                        .setDesiredHeight(ICONSIZE+2)
                        .setBorder(1)
                        .setBorderColor(0xff777777)
                        .setSelectable(true)
                        .addIconEvent(new IconEvent() {
                            @Override
                            public boolean iconArrives(IconHolder parent, IIcon icon) {
                                updateProgramCard();
                                return true;
                            }

                            @Override
                            public boolean iconLeaves(IconHolder parent, IIcon icon) {
                                return true;
                            }

                            @Override
                            public boolean iconClicked(IconHolder parent, IIcon icon, int dx, int dy) {
                                if (dy <= 3 && dx >= 10 && dx <= 14) {
                                    handleIconOverlay(icon, "top", 0, 5);
                                } else if (dy >= ICONSIZE-3 && dx >= 10 && dx <= 14) {
                                    handleIconOverlay(icon, "bot", 2, 5);
                                } else if (dx <= 3 && dy >= 10 && dy <= 14) {
                                    handleIconOverlay(icon, "lef", 3, 5);
                                } else if (dx >= ICONSIZE-3 && dy >= 10 && dy <= 14) {
                                    handleIconOverlay(icon, "rig", 1, 5);
                                }
                                System.out.println("dx = " + dx + "," + dy);
                                return true;
                            }
                        });
                rowPanel.addChild(holder);
            }
            gridList.addChild(rowPanel);
        }

//        int leftx = 0;
//        int topy = 0;
//        for (int x = 0 ; x < 13 ; x++) {
//            for (int y = 0 ; y < 6 ; y++) {
//                IconHolder holder = new IconHolder(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(leftx + x*19, topy + y*19, 18, 18)).setBorder(1);
//                panel.addChild(holder);
//            }
//        }

        panel.addChild(gridList).addChild(slider);

        return panel;
    }

    private void handleIconOverlay(IIcon icon, String prefix, int u, int v) {
        if (icon.hasOverlay(prefix+"_red")) {
            icon.removeOverlay(prefix+"_red");
            icon.addOverlay(new ImageIcon(prefix+"_green").setDimensions(ICONSIZE, ICONSIZE).setImage(icons, u*ICONSIZE, (v+1)*ICONSIZE));
        } else if (icon.hasOverlay(prefix+"_green")) {
            icon.removeOverlay(prefix+"_green");
        } else {
            icon.addOverlay(new ImageIcon(prefix+"_red").setDimensions(ICONSIZE, ICONSIZE).setImage(icons, u*ICONSIZE, v*ICONSIZE));
        }
        updateProgramCard();
    }

    private void clearGrid() {
        for (int x = 0 ; x < GRID_WIDTH ; x++) {
            for (int y = 0 ; y < GRID_HEIGHT ; y++) {
                getHolder(x, y).setIcon(null);
            }
        }
    }

    private void updateProgramCard() {

    }

    private void readProgramCard() {
        clearGrid();
        ItemStack card = tileEntity.getStackInSlot(ProgrammerContainer.SLOT_CARD);
        if (card == null) {
            return;
        }
        ProgramCardInstance instance = ProgramCardInstance.parseInstance(card);
        for (Map.Entry<Pair<Integer, Integer>, GridInstance> entry : instance.getGridInstances().entrySet()) {
            int x = entry.getKey().getLeft();
            int y = entry.getKey().getRight();
            GridInstance gridInstance = entry.getValue();

        }

    }

    private IconHolder getHolder(int x, int y) {
        Panel row = (Panel) gridList.getChild(y);
        return (IconHolder) row.getChild(x);
    }

    private Panel setupControlPanel() {
        return new Panel(mc, this).setLayout(new VerticalLayout()).setLayoutHint(new PositionalLayout.PositionalHint(26, 157, 58, 50))
                .addChild(new Button(mc, this).setText("Load").setDesiredHeight(15))
                .addChild(new Button(mc, this).setText("Save").setDesiredHeight(15))
                .addChild(new Button(mc, this).setText("Clear").setDesiredHeight(15));
    }

    private Panel setupListPanel() {
        WidgetList list = new WidgetList(mc, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(0, 0, 62, 220))
                .setPropagateEventsToChildren(true)
                .setInvisibleSelection(true)
                .setDrawHorizontalLines(false)
                .setRowheight(ICONSIZE+2);
        Slider slider = new Slider(mc, this)
                .setVertical()
                .setScrollable(list)
                .setLayoutHint(new PositionalLayout.PositionalHint(62, 0, 9, 220));

        int id = 1;
        for (int x = 0 ; x < 16 ; x++) {
            Panel childPanel = new Panel(mc, this).setLayout(new HorizontalLayout().setVerticalMargin(1).setSpacing(1).setHorizontalMargin(1)).setDesiredHeight(ICONSIZE+1);

            for (int y = 0 ; y < 3 ; y++) {
                IconHolder holder = new IconHolder(mc, this).setDesiredWidth(ICONSIZE).setDesiredHeight(ICONSIZE)
                        .setMakeCopy(true);
                holder.setIcon(ICONS.get("" + id));
                childPanel.addChild(holder);
                id++;
                if (id >= ICONS.size()) {
                    break;
                }
            }

            list.addChild(childPanel);
            if (id >= ICONS.size()) {
                break;
            }
        }

        return new Panel(mc, this).setLayout(new PositionalLayout()).setLayoutHint(new PositionalLayout.PositionalHint(5, 5, 72, 220))
                .addChild(list)
                .addChild(slider);
//                .setFilledRectThickness(-2)
//                .setFilledBackground(StyleConfig.colorListBackground);
    }

    private Panel createValuePanel(String labelName, String tempDefault) {
        Label label = (Label) new Label(mc, this)
                .setText(labelName)
                .setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT)
                .setDesiredHeight(13)
                .setLayoutHint(new PositionalLayout.PositionalHint(0, 0, 55, 13));
        TextField field = new TextField(mc, this)
                .setText(tempDefault)
                .setDesiredHeight(13)
                .setLayoutHint(new PositionalLayout.PositionalHint(0, 12, 49, 13));
        Button button = new Button(mc, this)
                .setText("...")
                .setDesiredHeight(13)
                .addButtonEvent(w -> openValueEditor())
                .setLayoutHint(new PositionalLayout.PositionalHint(50, 12, 11, 13));

        return new Panel(mc, this).setLayout(new PositionalLayout())
                .addChild(label)
                .addChild(field)
                .addChild(button)
                .setDesiredWidth(62);
    }

    private void openValueEditor() {
        Panel panel = new Panel(mc, this)
                .setLayout(new VerticalLayout())
                .setFilledBackground(0xff666666, 0xffaaaaaa);
        panel.setBounds(new Rectangle(50, 50, 200, 100));
        Window modalWindow = getWindowManager().createModalWindow(panel);
        panel.addChild(new Label(mc, this).setText("Label"));
        panel.addChild(new Button(mc, this)
                .addButtonEvent(w -> getWindowManager().closeWindow(modalWindow))
                .setText("Close"));
    }

    private Panel setupEditorPanel() {
        Panel slotPanel = createValuePanel("Slot:", "<var 3>");
        Panel amountPanel = createValuePanel("Amount:", "<64>");

        return new Panel(mc, this).setLayout(new HorizontalLayout()).setLayoutHint(new PositionalLayout.PositionalHint(4, 123, 249, 30))
                .addChild(slotPanel)
                .addChild(amountPanel)
                .setFilledRectThickness(-1)
                .setFilledBackground(StyleConfig.colorListBackground);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow();
    }
}
