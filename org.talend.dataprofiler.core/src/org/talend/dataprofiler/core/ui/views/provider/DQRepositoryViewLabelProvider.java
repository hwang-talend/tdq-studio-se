// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.views.provider;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.MDMConnectionItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.cwm.helper.ColumnHelper;
import org.talend.cwm.helper.ColumnSetHelper;
import org.talend.cwm.relational.TdColumn;
import org.talend.cwm.relational.TdTable;
import org.talend.cwm.relational.TdView;
import org.talend.cwm.xml.TdXmlElementType;
import org.talend.cwm.xml.TdXmlSchema;
import org.talend.dataprofiler.core.ImageLib;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.recycle.DQRecycleBinNode;
import org.talend.dataprofiler.core.recycle.IRecycleBin;
import org.talend.dataprofiler.ecos.model.IEcosCategory;
import org.talend.dataprofiler.ecos.model.IEcosComponent;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.domain.pattern.RegularExpression;
import org.talend.dataquality.indicators.definition.IndicatorCategory;
import org.talend.dataquality.indicators.definition.IndicatorDefinition;
import org.talend.dataquality.properties.TDQAnalysisItem;
import org.talend.dataquality.properties.TDQBusinessRuleItem;
import org.talend.dataquality.properties.TDQIndicatorDefinitionItem;
import org.talend.dataquality.properties.TDQPatternItem;
import org.talend.dataquality.properties.TDQReportItem;
import org.talend.dq.analysis.ColumnDependencyAnalysisHandler;
import org.talend.dq.helper.EObjectHelper;
import org.talend.dq.helper.PropertyHelper;
import org.talend.dq.helper.resourcehelper.AnaResourceFileHelper;
import org.talend.dq.nodes.foldernode.AbstractFolderNode;
import org.talend.dq.nodes.foldernode.IFolderNode;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * @author rli
 * 
 */
public class DQRepositoryViewLabelProvider extends AdapterFactoryLabelProvider {

    public DQRepositoryViewLabelProvider() {
        super(MNComposedAdapterFactory.getAdapterFactory());
    }

    public Image getImage(Object element) {

        if (element instanceof IFolderNode) {
            return ImageLib.getImage(ImageLib.FOLDERNODE_IMAGE);
        } else if (element instanceof TdColumn) {
            if (ColumnHelper.isPrimaryKey((TdColumn) element)) {
                // get the icon for primary key
                return ImageLib.getImage(ImageLib.PK_COLUMN);
            }
        } else if (element instanceof IEcosComponent) {
            return ImageLib.getImage(ImageLib.EXCHANGE);
        } else if (element instanceof IEcosCategory) {
            return ImageLib.getImage(ImageLib.EXCHANGE);
        } else if (element instanceof IndicatorCategory) {
            return ImageLib.getImage(ImageLib.IND_CATEGORY);
        } else if (element instanceof IndicatorDefinition) {
            return ImageLib.getImage(ImageLib.IND_DEFINITION);
        } else if (element instanceof TdView) {
            return ImageLib.getImage(ImageLib.VIEW);
        } else if (element instanceof TdXmlSchema) {
            return ImageLib.getImage(ImageLib.XML_DOC);
        } else if (element instanceof TdXmlElementType) {
            return ImageLib.getImage(ImageLib.XML_ELEMENT_DOC);
        } else if (element instanceof IRecycleBin) {
            return ImageLib.getImage(ImageLib.RECYCLEBIN_EMPTY);
        } else
        // MOD qiongli
        if (element instanceof DQRecycleBinNode) {
            DQRecycleBinNode rbn = (DQRecycleBinNode) element;
            Object obj = rbn.getObject();
            // MOD qiongli 2010-10-8,bug 15674
            if (obj instanceof Property) {
                Property property = (Property) obj;
                Item item = property.getItem();
                if (item instanceof TDQAnalysisItem) {
                    IFile file = PropertyHelper.getItemFile(property);
                    Analysis analysis = AnaResourceFileHelper.getInstance().findAnalysis(file);
                    ColumnDependencyAnalysisHandler analysisHandler = new ColumnDependencyAnalysisHandler();
                    analysisHandler.setAnalysis(analysis);
                    if (analysisHandler.getResultMetadata().getExecutionNumber() != 0) {
                        if (!analysisHandler.getResultMetadata().isLastRunOk()) {
                            return ImageLib.createErrorIcon(ImageLib.ANALYSIS_OBJECT).createImage();
                        } else if (analysisHandler.getResultMetadata().isOutThreshold()) {
                            return ImageLib.createInvalidIcon(ImageLib.ANALYSIS_OBJECT).createImage();
                        }
                    }
                    return ImageLib.getImage(ImageLib.ANALYSIS_OBJECT);
                } else if (item instanceof TDQReportItem) {
                    return ImageLib.getImage(ImageLib.REPORT_OBJECT);
                } else if (item instanceof TDQPatternItem) {
                    return ImageLib.getImage(ImageLib.PATTERN_REG);
                } else if (item instanceof TDQBusinessRuleItem) {
                    return ImageLib.getImage(ImageLib.DQ_RULE);
                } else if (item instanceof TDQIndicatorDefinitionItem) {
                    return ImageLib.getImage(ImageLib.IND_DEFINITION);
                } else if (item instanceof MDMConnectionItem) {
                    return ImageLib.getImage(ImageLib.MDM_CONNECTION);
                } else if (item instanceof ConnectionItem) {
                    return ImageLib.getImage(ImageLib.TD_DATAPROVIDER);
                }
            } else if (obj instanceof IFolder) {
                return ImageLib.getImage(ImageLib.FOLDERNODE_IMAGE);
            }
        } else if (element instanceof IRepositoryViewObject) {
            IRepositoryViewObject conn = (IRepositoryViewObject) element;
            // Currently we only care about connection Item.
            Item connItem = conn.getProperty().getItem();
            if (connItem instanceof MDMConnectionItem) {
                return ImageLib.getImage(ImageLib.MDM_CONNECTION);
            } else if (connItem instanceof ConnectionItem) {
                return ImageLib.getImage(ImageLib.TD_DATAPROVIDER);
            }
        } else if (element instanceof MDMConnection) {
            return ImageLib.getImage(ImageLib.MDM_CONNECTION);
        }
        // ~

        return super.getImage(element);
    }

    public String getText(Object element) {
        String tableOwner = null;
        if (element instanceof ModelElement) {
            tableOwner = ColumnSetHelper.getTableOwner((ModelElement) element);
        }
        if (element instanceof AbstractFolderNode) {
            if (((IFolderNode) element).getChildren() != null) {
                return ((IFolderNode) element).getName() + "(" + ((IFolderNode) element).getChildren().length + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }

            return ((IFolderNode) element).getName();
        } else if (element instanceof IEcosComponent) {
            return ((IEcosComponent) element).getName();
        } else if (element instanceof IEcosCategory) {
            return ((IEcosCategory) element).getName();
        } else if (element instanceof IndicatorDefinition) {
            return ((IndicatorDefinition) element).getName();
        } else if (element instanceof IndicatorCategory) {
            return ((IndicatorCategory) element).getName();
        } else if (element instanceof IRecycleBin) {
            return ((IRecycleBin) element).getName();
        }

        // PTODO qzhang fixed bug 4176: Display expressions as children of the
        // patterns
        if (element instanceof RegularExpression) {
            RegularExpression regExp = (RegularExpression) element;
            return regExp.getExpression().getLanguage();
        } else if (element instanceof Connection) {
            return ((Connection) element).getName();
        }

        // MOD mzhao feature 10238
        if (element instanceof TdXmlSchema) {
            return ((TdXmlSchema) element).getName();
        } else if (element instanceof TdXmlElementType) {
            String elemLabe = ((TdXmlElementType) element).getName();
            String elementType = ((TdXmlElementType) element).getJavaType();
            if (elementType != null && !StringUtils.isEmpty(elementType)) {
                elemLabe += " (" + elementType + ")";
            }
            return elemLabe;
        } else if ((element instanceof TdTable || element instanceof TdView) && tableOwner != null && !"".equals(tableOwner)) {
            return super.getText(element) + "(" + tableOwner + ")";
        } else
        // MOD qiongli :get the name of recycle bin's child
        if (element instanceof DQRecycleBinNode) {
            DQRecycleBinNode rbn = (DQRecycleBinNode) element;
            Object obj = rbn.getObject();
            // MOD qiongli 2010-8-10,bug 15674
            if (obj instanceof Property) {
                Property property = (Property) obj;
                Item item = property.getItem();
                if (item instanceof ConnectionItem) {
                    Connection connection = ((ConnectionItem) item).getConnection();
                    if (connection.eIsProxy()) {
                        connection = (Connection) EObjectHelper.resolveObject(connection);
                    }
                    return connection.getName();
                }
                return property.getLabel();
            } else if (obj instanceof IFolder) {
                return ((IFolder) obj).getName();
            }

        } else if (element instanceof IRepositoryViewObject) {
            // MOD mzhao feature 2010-08-12 14891: use same repository API with TOS to persistent metadata
            IRepositoryViewObject conn = (IRepositoryViewObject) element;
            return conn.getLabel();
            // // Currently we only care about connection Item.
            // Item connItem = conn.getProperty().getItem();
            // if (connItem instanceof ConnectionItem) {
            // return ((ConnectionItem) connItem).getConnection().getName();
            // }
        }
        String text = super.getText(element);
        return "".equals(text) ? DefaultMessagesImpl.getString("DQRepositoryViewLabelProvider.noName") : text;
    }
}
