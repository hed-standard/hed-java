//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.utsa.tagger;

import edu.utsa.tagger.gui.FieldOrderView;
import edu.utsa.tagger.gui.FieldSelectView;
import edu.utsa.tagger.gui.TaggerView;

public interface IFactory {
    AbstractEventModel createAbstractEventModel(Tagger var1);

    AbstractTagModel createAbstractTagModel(Tagger var1);

    TaggerView createTaggerView(TaggerLoader var1, Tagger var2, String var3);

    TaggerView createTaggerView(TaggerLoader var1);

    FieldSelectView createFieldSelectView(FieldSelectLoader var1, String var2, String[] var3, String[] var4, String var5);

    FieldOrderView createFieldOrderView(FieldOrderLoader var1, String var2, String[] var3);
}
