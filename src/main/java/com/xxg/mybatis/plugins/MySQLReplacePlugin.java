package com.xxg.mybatis.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.*;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.Iterator;
import java.util.List;

/**
 * Created by wucao on 2018/11/16.
 */
public class MySQLReplacePlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * 生成replace和replaceSelective方法
     */
    @Override
    public boolean clientGenerated(Interface interfaze,
                                   TopLevelClass topLevelClass,
                                   IntrospectedTable introspectedTable) {
        Method replaceSelective = new Method();
        replaceSelective.setReturnType(FullyQualifiedJavaType.getIntInstance());
        replaceSelective.setName("replaceSelective");

        Method replace = new Method();
        replace.setReturnType(FullyQualifiedJavaType.getIntInstance());
        replace.setName("replace");

        FullyQualifiedJavaType parameterType = introspectedTable.getRules().calculateAllFieldsClass();
        replaceSelective.addParameter(new Parameter(parameterType, "record"));
        replace.addParameter(new Parameter(parameterType, "record"));

        interfaze.addMethod(replaceSelective);
        interfaze.addMethod(replace);

        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document,
                                           IntrospectedTable introspectedTable) {

        XmlElement root = document.getRootElement();
        root.addElement(sqlMapReplaceElementGenerated(introspectedTable));
        root.addElement(sqlMapReplaceSelectiveElementGenerated(introspectedTable));
        return true;
    }

    private XmlElement sqlMapReplaceElementGenerated(IntrospectedTable introspectedTable) {
        // <update id="replace">...</update>
        XmlElement replace = new XmlElement("update");
        replace.addAttribute(new Attribute("id", "replace"));

        StringBuilder replaceClause = new StringBuilder();
        StringBuilder valuesClause = new StringBuilder();

        replaceClause.append("replace into ");
        replaceClause.append(introspectedTable.getFullyQualifiedTableNameAtRuntime()); // 表名
        replaceClause.append(" (");
        valuesClause.append("values (");

        // 遍历所有列
        Iterator<IntrospectedColumn> iter = introspectedTable.getAllColumns().iterator(); // 所有列
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();

            replaceClause.append(MyBatis3FormattingUtilities
                    .getEscapedColumnName(introspectedColumn));
            valuesClause.append(MyBatis3FormattingUtilities
                    .getParameterClause(introspectedColumn));
            if (iter.hasNext()) {
                replaceClause.append(", ");
                valuesClause.append(", ");
            }
        }

        replaceClause.append(')');
        valuesClause.append(')');

        replace.addElement(new TextElement(replaceClause.toString() + ' ' + valuesClause.toString()));
        return replace;
    }

    private XmlElement sqlMapReplaceSelectiveElementGenerated(IntrospectedTable introspectedTable) {
        // <update id="replaceSelective">...</update>
        XmlElement replace = new XmlElement("update");
        replace.addAttribute(new Attribute("id", "replaceSelective"));

        replace.addElement(new TextElement("replace into "
                + introspectedTable.getFullyQualifiedTableNameAtRuntime()));

        XmlElement replaceTrimElement = new XmlElement("trim");
        replaceTrimElement.addAttribute(new Attribute("prefix", "("));
        replaceTrimElement.addAttribute(new Attribute("suffix", ")"));
        replaceTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
        replace.addElement(replaceTrimElement);

        XmlElement valuesTrimElement = new XmlElement("trim");
        valuesTrimElement.addAttribute(new Attribute("prefix", "values ("));
        valuesTrimElement.addAttribute(new Attribute("suffix", ")"));
        valuesTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
        replace.addElement(valuesTrimElement);

        // 遍历所有列
        for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {

            XmlElement replaceNotNullElement = new XmlElement("if");
            replaceNotNullElement.addAttribute(new Attribute("test",
                    introspectedColumn.getJavaProperty() + " != null"));
            replaceNotNullElement.addElement(new TextElement(MyBatis3FormattingUtilities
                    .getEscapedColumnName(introspectedColumn) + ','));
            replaceTrimElement.addElement(replaceNotNullElement);

            XmlElement valuesNotNullElement = new XmlElement("if");
            valuesNotNullElement.addAttribute(new Attribute("test",
                    introspectedColumn.getJavaProperty() + " != null"));
            valuesNotNullElement.addElement(new TextElement(MyBatis3FormattingUtilities
                    .getParameterClause(introspectedColumn) + ','));
            valuesTrimElement.addElement(valuesNotNullElement);
        }
        return replace;
    }

}
