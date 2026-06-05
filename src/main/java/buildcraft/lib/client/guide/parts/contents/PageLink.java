package buildcraft.lib.client.guide.parts.contents;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

import net.minecraft.server.permissions.Permissions;

import net.minecraft.world.entity.player.Player;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuideText;

public abstract class PageLink implements IContentsLeaf {

    public final PageLine text;
    public final boolean startVisible;

    public final boolean creativeOnly;
    private final String lowerCaseName;
    private boolean visible;

    private int sortIndex = 0;

    public PageLink(PageLine text, boolean startVisible) {
        this(text, startVisible, false);
    }

    public PageLink(PageLine text, boolean startVisible, boolean creativeOnly) {
        this.text = text;
        this.startVisible = startVisible;
        this.creativeOnly = creativeOnly;
        lowerCaseName = text.text.toLowerCase(Locale.ROOT);
        visible = startVisible && (!creativeOnly || canAccessCreativeOnlyContent());
    }

    public static boolean canAccessCreativeOnlyContent() {
        Minecraft mc = Minecraft.getInstance();

        if (mc == null) return false;
        Player p = mc.player;
        if (p == null) return false;
        if (p.getAbilities().instabuild) return true;

        if (p.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) return true;

        MinecraftServer sp = mc.getSingleplayerServer();
        return sp != null && sp.getWorldData().isAllowCommands();
    }

    @Override
    public String getSearchName() {
        return lowerCaseName;
    }

    @Override
    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

    @Nullable
    protected List<String> getTooltip() {
        return null;
    }

    public void appendTooltip(GuiGuide gui) {
        List<String> tooltip = getTooltip();
        if (tooltip != null && !tooltip.isEmpty()) {
            gui.tooltips.add(tooltip);
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(Set<PageLink> matches) {
        visible = matches.contains(this) && (!creativeOnly || canAccessCreativeOnlyContent());
    }

    @Override
    public void resetVisibility() {
        visible = startVisible && (!creativeOnly || canAccessCreativeOnlyContent());
    }

    @Override
    public GuidePart createGuidePart(GuiGuide gui) {
        return new GuideText(gui, text) {
            @Override
            protected void renderTooltip() {
                appendTooltip(gui);
            }
        };
    }

    public abstract GuidePageFactory getFactoryLink();
}
