// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dq.analysis.match;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.commons.exception.BusinessException;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.dataquality.indicators.columnset.RecordMatchingIndicator;
import org.talend.dataquality.matchmerge.Attribute;
import org.talend.dataquality.record.linkage.genkey.AbstractGenerateKey;
import org.talend.dataquality.record.linkage.grouping.AnalysisMatchRecordGrouping;
import org.talend.dataquality.record.linkage.grouping.MatchGroupResultConsumer;
import org.talend.dataquality.record.linkage.grouping.swoosh.RichRecord;
import org.talend.dataquality.record.linkage.iterator.ResultSetIterator;
import org.talend.dataquality.record.linkage.utils.MatchAnalysisConstant;
import org.talend.dq.analysis.AnalysisRecordGroupingUtils;

/**
 * For each record: find its related block, and use this block's matching to do the match.
 * 
 */
public class BlockAndMatchManager {

    private Map<String, OneBlockMatching> blockMatchMap = new HashMap<String, OneBlockMatching>();

    // Used to read each record from the data source
    private ResultSetIterator resultIterator;

    private MatchGroupResultConsumer matchResultConsumer;

    private RecordMatchingIndicator recordMatchingIndicator;

    private Map<MetadataColumn, String> columnMap;

    private AbstractGenerateKey blockKeyGenerator = new AbstractGenerateKey();

    private Map<String, String> colName2IndexMap = new HashMap<String, String>();

    private List<Map<String, String>> blockKeyDefinition;

    public BlockAndMatchManager(ResultSetIterator resultIterator, MatchGroupResultConsumer matchResultConsumer,
            Map<MetadataColumn, String> columnMap, RecordMatchingIndicator recordMatchingIndicator) {
        this.resultIterator = resultIterator;
        this.matchResultConsumer = matchResultConsumer;
        this.recordMatchingIndicator = recordMatchingIndicator;
        this.columnMap = columnMap;
        for (MetadataColumn metaCol : columnMap.keySet()) {
            colName2IndexMap.put(metaCol.getName(), columnMap.get(metaCol));
        }
        initBlockKeyDefinitions();
    }

    public void initBlockKeyDefinitions() {
        // By default for analysis, the applied blocking key will be the key from key generation definition. This
        // will be refined when there is a need to define the applied blocking key manually by user later.
        AnalysisRecordGroupingUtils.createAppliedBlockKeyByGenKey(recordMatchingIndicator);

        blockKeyDefinition = AnalysisRecordGroupingUtils.getBlockKeySchema(recordMatchingIndicator);
    }

    public void run() throws BusinessException {
        while (this.resultIterator.hasNext()) {
            RichRecord record = (RichRecord) this.resultIterator.next();
            if (record == null) {
                continue;
            }
            // get the block key of this record(which block this record belongs)
            String blockKey = getBlockKey(record);
            OneBlockMatching matchInOneBlock = this.blockMatchMap.get(blockKey);
            if (matchInOneBlock == null) {// create a new matching for this new block to do the matching
                matchInOneBlock = createBlockKeyManager(blockKey);
            }
            // do group in the same block
            matchInOneBlock.run(record);
        }

        // end all
        for (String key : this.blockMatchMap.keySet()) {
            OneBlockMatching oneBlockMatching = blockMatchMap.get(key);
            oneBlockMatching.end();
        }
    }

    /**
     * DOC yyin Comment method "createBlockKeyManager".
     * 
     * @param blockKey
     * @return
     * @throws BusinessException
     */
    private OneBlockMatching createBlockKeyManager(String blockKey) throws BusinessException {
        OneBlockMatching matchInOneBlock = new OneBlockMatching();
        this.blockMatchMap.put(blockKey, matchInOneBlock);
        return matchInOneBlock;
    }

    /**
     * DOC yyin Comment method "getBlockKey".
     * 
     * @param record
     * @return
     */
    private String getBlockKey(RichRecord record) {
        Map<String, String> columnValueMap = new HashMap<String, String>();
        for (String columnName : colName2IndexMap.keySet()) {
            int index = Integer.parseInt(colName2IndexMap.get(columnName));
            columnValueMap.put(columnName, record.getAttributes().get(index).getValue());
        }

        String blockkey = blockKeyGenerator.getGenKey(blockKeyDefinition, columnValueMap);
        // add the block key into the record
        Attribute attribute = new Attribute(MatchAnalysisConstant.BLOCK_KEY);
        attribute.setValue(blockkey);
        record.getAttributes().add(attribute);

        return blockkey;
    }

    /**
     * the match grouping within one block.
     */
    class OneBlockMatching {

        private AnalysisMatchRecordGrouping analysisMatchRecordGrouping = new AnalysisMatchRecordGrouping(matchResultConsumer);

        public OneBlockMatching() throws BusinessException {
            AnalysisRecordGroupingUtils.setRuleMatcher(columnMap, recordMatchingIndicator, analysisMatchRecordGrouping);
            try {
                AnalysisRecordGroupingUtils.initialMatchGrouping(columnMap, recordMatchingIndicator, analysisMatchRecordGrouping);
            } catch (InstantiationException e1) {
                throw new BusinessException();
            } catch (IllegalAccessException e1) {
                throw new BusinessException();
            } catch (ClassNotFoundException e1) {
                throw new BusinessException();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Thread#run()
         */
        public void run(RichRecord currentRecord) throws BusinessException {
            String[] inputStrRow = new String[currentRecord.getAttributes().size()];
            int index = 0;
            for (Attribute obj : currentRecord.getAttributes()) {
                inputStrRow[index++] = obj.getValue() == null ? null : obj.getValue().toString();
            }
            try {
                analysisMatchRecordGrouping.doGroup(inputStrRow);
            } catch (IOException e) {
                throw new BusinessException();
            } catch (InterruptedException e) {
                throw new BusinessException();
            }
        }

        public void end() throws BusinessException {
            try {
                analysisMatchRecordGrouping.end();
            } catch (IOException e) {
                throw new BusinessException();
            } catch (InterruptedException e) {
                throw new BusinessException();
            }
        }
    }
}