/*
 *  Copyright 2010 The MyBatis Team
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.mybatis.generator.codegen.mybatis3.javamapper.elements.sqlprovider;

import static org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities.getEscapedColumnName;
import static org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities.getParameterClause;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getGetterMethodName;
import static org.mybatis.generator.internal.util.StringUtility.escapeStringForJava;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * 
 * @author Jeff Butler
 * 
 */
public class ProviderInsertSelectiveMethodGenerator extends
        AbstractJavaProviderMethodGenerator {

    public ProviderInsertSelectiveMethodGenerator() {
        super();
    }

    @Override
    public void addClassElements(TopLevelClass topLevelClass) {
        topLevelClass.addStaticImport("org.apache.ibatis.jdbc.SqlBuilder.BEGIN"); //$NON-NLS-1$
        topLevelClass.addStaticImport("org.apache.ibatis.jdbc.SqlBuilder.INSERT_INTO"); //$NON-NLS-1$
        topLevelClass.addStaticImport("org.apache.ibatis.jdbc.SqlBuilder.SQL"); //$NON-NLS-1$
        topLevelClass.addStaticImport("org.apache.ibatis.jdbc.SqlBuilder.VALUES"); //$NON-NLS-1$

        FullyQualifiedJavaType fqjt = introspectedTable.getRules()
            .calculateAllFieldsClass();
        topLevelClass.addImportedType(fqjt);

        Method method = new Method(
                introspectedTable.getInsertSelectiveStatementId());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getStringInstance());
        method.addParameter(new Parameter(fqjt, "record")); //$NON-NLS-1$
        
        context.getCommentGenerator().addGeneralMethodComment(method,
                introspectedTable);

        method.addBodyLine("BEGIN();"); //$NON-NLS-1$
        method.addBodyLine(String.format("INSERT_INTO(\"%s\");", //$NON-NLS-1$
                escapeStringForJava(introspectedTable.getFullyQualifiedTableNameAtRuntime())));

        for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
            if (introspectedColumn.isIdentity()) {
                // cannot set values on identity fields
                continue;
            }
            
            method.addBodyLine(""); //$NON-NLS-1$
            if (!introspectedColumn.getFullyQualifiedJavaType().isPrimitive()
                    && !introspectedColumn.isSequenceColumn()) {
                method.addBodyLine(String.format("if (record.%s() != null) {", //$NON-NLS-1$
                    getGetterMethodName(introspectedColumn.getJavaProperty(),
                            introspectedColumn.getFullyQualifiedJavaType())));
            }
            method.addBodyLine(String.format("VALUES(\"%s\", \"%s\");", //$NON-NLS-1$
                    escapeStringForJava(getEscapedColumnName(introspectedColumn)),
                    getParameterClause(introspectedColumn)));
            if (!introspectedColumn.getFullyQualifiedJavaType().isPrimitive()
                    && !introspectedColumn.isSequenceColumn()) {
                method.addBodyLine("}"); //$NON-NLS-1$
            }
        }
        
        method.addBodyLine(""); //$NON-NLS-1$
        method.addBodyLine("return SQL();"); //$NON-NLS-1$
        
        topLevelClass.addMethod(method);
    }
}