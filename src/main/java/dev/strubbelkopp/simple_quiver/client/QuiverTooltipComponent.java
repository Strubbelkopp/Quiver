package dev.strubbelkopp.simple_quiver.client;

import dev.strubbelkopp.simple_quiver.item.QuiverItem;
import dev.strubbelkopp.simple_quiver.item.QuiverTooltipData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

@Environment(EnvType.CLIENT)
public class QuiverTooltipComponent implements TooltipComponent {

    public static final Identifier TEXTURE = new Identifier("textures/gui/container/bundle.png");
    private final DefaultedList<ItemStack> inventory;
    private final int occupancy;
    private final int activeArrowIndex;

    public QuiverTooltipComponent(QuiverTooltipData data) {
        this.inventory = data.getInventory();
        this.occupancy = data.getQuiverOccupancy();
        this.activeArrowIndex = data.getActiveArrowIndex();
    }

    @Override
    public int getHeight() {
        return this.getRows() * 20 + 2 + 4;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return this.getColumns() * 18 + 2;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        int columns = this.getColumns();
        int rows = this.getRows();
        boolean isFull = this.occupancy >= QuiverItem.MAX_STORAGE;
        int i = 0;
        for(int row = 0; row < rows; ++row) {
            for(int column = 0; column < columns; ++column) {
                int u = x + column * 18 + 1;
                int v = y + row * 20 + 1;
                this.drawSlot(u, v, i++, isFull, context, textRenderer);
            }
        }
        this.drawOutline(x, y, columns, rows, context);
    }

    private void drawSlot(int x, int y, int index, boolean shouldBlock, DrawContext context, TextRenderer textRenderer) {
        if (index >= this.inventory.size()) {
            this.draw(context, x, y, shouldBlock ? QuiverTooltipComponent.Sprite.BLOCKED_SLOT : QuiverTooltipComponent.Sprite.SLOT);
        } else {
            ItemStack itemStack = this.inventory.get(index);
            this.draw(context, x, y, QuiverTooltipComponent.Sprite.SLOT);
            context.drawItem(itemStack, x + 1, y + 1, index);
            context.drawItemInSlot(textRenderer, itemStack, x + 1, y + 1);
            if (index == this.activeArrowIndex) {
                HandledScreen.drawSlotHighlight(context, x + 1, y + 1, 0);
            }
        }
    }

    private void drawOutline(int x, int y, int columns, int rows, DrawContext context) {
        this.draw(context, x, y, QuiverTooltipComponent.Sprite.BORDER_CORNER_TOP);
        this.draw(context, x + columns * 18 + 1, y, QuiverTooltipComponent.Sprite.BORDER_CORNER_TOP);

        for(int i = 0; i < columns; ++i) {
            this.draw(context, x + 1 + i * 18, y, QuiverTooltipComponent.Sprite.BORDER_HORIZONTAL_TOP);
            this.draw(context, x + 1 + i * 18, y + rows * 20, QuiverTooltipComponent.Sprite.BORDER_HORIZONTAL_BOTTOM);
        }

        for(int i = 0; i < rows; ++i) {
            this.draw(context, x, y + i * 20 + 1, QuiverTooltipComponent.Sprite.BORDER_VERTICAL);
            this.draw(context, x + columns * 18 + 1, y + i * 20 + 1, QuiverTooltipComponent.Sprite.BORDER_VERTICAL);
        }

        this.draw(context, x, y + rows * 20, QuiverTooltipComponent.Sprite.BORDER_CORNER_BOTTOM);
        this.draw(context, x + columns * 18 + 1, y + rows * 20, QuiverTooltipComponent.Sprite.BORDER_CORNER_BOTTOM);
    }

    private void draw(DrawContext context, int x, int y, QuiverTooltipComponent.Sprite sprite) {
        context.drawTexture(TEXTURE, x, y, 0, (float)sprite.u, (float)sprite.v, sprite.width, sprite.height, 128, 128);
    }

    private int getColumns() {
        return Math.max(2, (int)Math.ceil(Math.sqrt((double)this.inventory.size() + 1.0)));
    }

    private int getRows() {
        return (int)Math.ceil(((double)this.inventory.size() + 1.0) / (double)this.getColumns());
    }

    @Environment(EnvType.CLIENT)
    enum Sprite {
        SLOT(0, 0, 18, 20),
        BLOCKED_SLOT(0, 40, 18, 20),
        BORDER_VERTICAL(0, 18, 1, 20),
        BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
        BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
        BORDER_CORNER_TOP(0, 20, 1, 1),
        BORDER_CORNER_BOTTOM(0, 60, 1, 1);

        public final int u;
        public final int v;
        public final int width;
        public final int height;

        Sprite(int u, int v, int width, int height) {
            this.u = u;
            this.v = v;
            this.width = width;
            this.height = height;
        }
    }
}
