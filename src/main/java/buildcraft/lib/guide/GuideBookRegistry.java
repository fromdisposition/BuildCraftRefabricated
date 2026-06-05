package buildcraft.lib.guide;

import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

import buildcraft.lib.script.ScriptableRegistry;

public class GuideBookRegistry extends ScriptableRegistry<GuideBook> {

    public static final GuideBookRegistry INSTANCE = new GuideBookRegistry();

    private GuideBookRegistry() {
        super(PackType.DATA_PACK, "buildcraft/book");
        addCustomType("", GuideBook.DESERIALISER);
    }

    @Nullable
    public GuideBook getBook(String bookName) {
        Identifier loc = Identifier.parse(bookName);

        GuideBook guideBook = getReloadableEntryMap().get(loc);
        if (guideBook != null) {
            return guideBook;
        }
        for (GuideBook book : getPermanent()) {
            if (book.name.toString().equals(bookName)) {
                return book;
            }
        }
        return null;
    }
}
