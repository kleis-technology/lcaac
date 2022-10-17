package com.github.albanseurat.lcaplugin.language.psi.stub

import com.github.albanseurat.lcaplugin.LcaLanguage
import com.github.albanseurat.lcaplugin.language.psi.PsiProductElement
import com.github.albanseurat.lcaplugin.psi.LcaTokenType
import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.github.albanseurat.lcaplugin.psi.impl.LcaProductExchangeImpl
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.lang.LighterASTTokenNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.stubs.*

class ProductStubElementType(debugName: String) : ILightStubElementType<ProductStub,
        PsiProductElement>(debugName, LcaLanguage.INSTANCE) {
    override fun getExternalId(): String = "lca.${super.toString()}"

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ProductStub {
        return ProductStubImpl(parentStub as StubElement<PsiProductElement>, dataStream.readNameString());
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ProductStub {
        val keyNode = LightTreeUtil.firstChildOfType(tree, node, LcaTypes.IDENTIFIER) as LighterASTTokenNode
        return ProductStubImpl(
            parentStub as StubElement<PsiProductElement>,
            tree.charTable.intern(keyNode.text).toString()
        );
    }

    override fun createStub(psi: PsiProductElement, parentStub: StubElement<out PsiElement>?): ProductStub {
        return ProductStubImpl(parentStub as StubElement<PsiProductElement>, psi.name)
    }

    override fun createPsi(stub: ProductStub): PsiProductElement {
        return LcaProductExchangeImpl(stub, this);
    }

    override fun indexStub(stub: ProductStub, sink: IndexSink) {
        sink.occurrence(LcaSubIndexKeys.PRODUCTS, stub.productName!!);
    }

    override fun serialize(stub: ProductStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.productName);
    }

}

/*

class PropertyStubElementType extends ILightStubElementType<PropertyStub, Property> {
  PropertyStubElementType() {
    super("PROPERTY", PropertiesElementTypes.LANG);
  }

  @Override
  public Property createPsi(@NotNull final PropertyStub stub) {
    return new PropertyImpl(stub, this);
  }

  @Override
  @NotNull
  public PropertyStub createStub(@NotNull final Property psi, final StubElement parentStub) {
    return new PropertyStubImpl(parentStub, psi.getKey());
  }

  @Override
  @NotNull
  public String getExternalId() {
    return "properties.prop";
  }

  @Override
  public void serialize(@NotNull final PropertyStub stub, @NotNull final StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getKey());
  }

  @Override
  @NotNull
  public PropertyStub deserialize(@NotNull final StubInputStream dataStream, final StubElement parentStub) throws IOException {
    return new PropertyStubImpl(parentStub, dataStream.readNameString());
  }

  @Override
  public void indexStub(@NotNull final PropertyStub stub, @NotNull final IndexSink sink) {
    sink.occurrence(PropertyKeyIndex.KEY, PropertyImpl.unescape(stub.getKey()));
  }

  @NotNull
  @Override
  public PropertyStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement parentStub) {
    LighterASTNode keyNode = LightTreeUtil.firstChildOfType(tree, node, PropertiesTokenTypes.KEY_CHARACTERS);
    String key = intern(tree.getCharTable(), keyNode);
    return new PropertyStubImpl(parentStub, key);
  }

  public static String intern(@NotNull CharTable table, @NotNull LighterASTNode node) {
    assert node instanceof LighterASTTokenNode : node;
    return table.intern(((LighterASTTokenNode)node).getText()).toString();
  }
}
 */