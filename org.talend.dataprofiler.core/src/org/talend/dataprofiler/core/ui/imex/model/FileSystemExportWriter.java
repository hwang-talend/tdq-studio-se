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
import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.talend.commons.emf.FactoriesUtil;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.dataprofiler.core.PluginChecker;
import org.talend.resource.ResourceManager;

/**
 * DOC bZhou class global comment. Detailled comment
 */
public class FileSystemExportWriter implements IImexWriter {

    private static Logger log = Logger.getLogger(FileSystemExportWriter.class);

    private String destination;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataprofiler.core.ui.imex.model.IImexWriter#populate(org.talend.dataprofiler.core.ui.imex.model.ItemRecord
     * [], boolean)
     */
    public ItemRecord[] populate(ItemRecord[] elements, boolean checkExisted) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataprofiler.core.ui.imex.model.IImexWriter#write(org.talend.dataprofiler.core.ui.imex.model.ItemRecord
     * , java.lang.String)
     */
    public void write(ItemRecord recored, String destination) throws IOException, CoreException {
        this.destination = destination;

        IPath itemDesPath = new Path(destination).append(recored.getFullPath());
        IPath propDesPath = itemDesPath.removeFileExtension().addFileExtension(FactoriesUtil.PROPERTIES_EXTENSION);

        // export item file
        File resItemFile = recored.getFile();
        File desItemFile = itemDesPath.toFile();

        copyFile(resItemFile, desItemFile);

        // export property file
        File resPropFile = recored.getPropertyPath().toFile();
        File desPropFile = propDesPath.toFile();

        copyFile(resPropFile, desPropFile);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.dataprofiler.core.ui.imex.model.IImexWriter#finish(org.talend.dataprofiler.core.ui.imex.model.ItemRecord
     * [])
     */
    public void finish(ItemRecord[] records) throws IOException {
        if (PluginChecker.isTDCPLoaded()) {
            IPath projResPath = ResourceManager.getRootProject().getLocation().append("talend.project");
            IPath projDesPath = new Path(destination).append(ResourceManager.getRootProjectName()).append("talend.project");
            File projFile = projResPath.toFile();
            if (projFile.exists()) {
                copyFile(projFile, projDesPath.toFile());
            }
        }
    }

    /**
     * DOC bZhou Comment method "copyFile".
     * 
     * @param source
     * @param target
     * @throws IOException
     */
    static void copyFile(File source, File target) throws IOException {
        if (source.exists()) {
            FilesUtils.copyFile(source, target);
        } else {
            log.warn("Export failed! " + source.getAbsolutePath() + " is not existed");
        }
    }
}
