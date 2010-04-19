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
package org.talend.dataprofiler.core.ui.imex.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.talend.commons.bridge.ReponsitoryContextBridge;
import org.talend.commons.emf.FactoriesUtil;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.PropertiesPackage;
import org.talend.core.model.properties.Property;
import org.talend.dq.helper.EObjectHelper;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * DOC bZhou class global comment. Detailled comment
 */
public class ItemRecord {

    private File file;

    private String projectName;

    private ModelElement element;

    private Property property;

    private List<String> errors = new ArrayList<String>();

    private static ResourceSet resourceSet;

    public ItemRecord(File file) {
        this.file = file;

        init();
    }

    /**
     * DOC bZhou Comment method "init".
     */
    private void init() {
        if (resourceSet == null) {
            resourceSet = new ResourceSetImpl();
        }

        if (element == null && file != null) {
            URI fileURI = URI.createFileURI(file.getAbsolutePath());
            Resource resource = resourceSet.getResource(fileURI, true);
            EList<EObject> contents = resource.getContents();
            if (contents != null && !contents.isEmpty()) {
                EObject object = contents.get(0);
                if (object instanceof ModelElement) {
                    element = (ModelElement) object;
                }
            }
        }

        if (property == null && file != null) {
            property = (Property) EObjectHelper.retrieveEObject(getPropertyPath(), PropertiesPackage.eINSTANCE.getProperty());
        }
    }

    /**
     * DOC bZhou Comment method "getFilePath".
     * 
     * @return
     */
    public IPath getFilePath() {
        return new Path(file.getAbsolutePath());
    }

    /**
     * DOC bZhou Comment method "getPropertyPath".
     * 
     * @return
     */
    public IPath getPropertyPath() {
        if (file != null) {
            IPath itemResPath = new Path(file.getAbsolutePath());
            return itemResPath.removeFileExtension().addFileExtension(FactoriesUtil.PROPERTIES_EXTENSION);
        }
        return null;
    }

    /**
     * DOC bZhou Comment method "getFullPath".
     * 
     * @return
     */
    public IPath getFullPath() {
        if (file.isFile()) {
            IPath path = new Path(file.getAbsolutePath());
            path = path.makeRelativeTo(ResourcesPlugin.getWorkspace().getRoot().getLocation());
            return path;
        }
        return null;
    }

    /**
     * DOC bZhou Comment method "getProjectName".
     * 
     * @return
     */
    public String getProjectName() {
        if (projectName == null && property != null) {

            InternalEObject author = (InternalEObject) property.getAuthor();
            if (author != null) {
                Resource projResource = author.eResource();
                if (projResource != null) {
                    URI projectUri = projResource.getURI();
                    IPath projectPath = new Path(projectUri.toFileString());
                    if (projectPath.toFile().exists()) {
                        Object projOBJ = EObjectHelper.retrieveEObject(projectPath, PropertiesPackage.eINSTANCE.getProject());
                        if (projOBJ != null) {
                            Project project = (Project) projOBJ;
                            this.projectName = project.getLabel();
                        }
                    }
                }
            }
        }
        return this.projectName == null ? ReponsitoryContextBridge.PROJECT_DEFAULT_NAME : this.projectName;
    }

    /**
     * clear the resource set.
     */
    public void clear() {

        if (resourceSet != null) {
            for (Resource resource : resourceSet.getResources()) {
                resource.unload();
            }
            resourceSet.getResources().clear();
            resourceSet = null;
        }

        element = null;
    }

    /**
     * DOC bZhou Comment method "addError".
     * 
     * @param error
     */
    public void addError(String error) {
        this.errors.add(error);
    }

    /**
     * Getter for file.
     * 
     * @return the file
     */
    public File getFile() {
        return this.file;
    }

    /**
     * Getter for errors.
     * 
     * @return the errors
     */
    public List<String> getErrors() {
        return this.errors;
    }

    /**
     * DOC bZhou Comment method "isValid".
     * 
     * @return
     */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /**
     * Getter for property.
     * 
     * @return the property
     */
    public Property getProperty() {
        return this.property;
    }

    /**
     * Getter for resourceSet.
     * 
     * @return the resourceSet
     */
    public ResourceSet getResourceSet() {
        return resourceSet;
    }

    /**
     * Getter for element.
     * 
     * @return the element
     */
    public ModelElement getElement() {
        return this.element;
    }
}
