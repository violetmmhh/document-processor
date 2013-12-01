package org.shirdrn.document.processor.component;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.document.processor.common.AbstractDatasetManager;
import org.shirdrn.document.processor.common.Context;
import org.shirdrn.document.processor.common.DocumentAnalyzer;
import org.shirdrn.document.processor.common.Term;
import org.shirdrn.document.processor.utils.ReflectionUtils;

public abstract class AbstractDocumentWordsCollector extends AbstractDatasetManager {
	
	private static final Log LOG = LogFactory.getLog(AbstractDocumentWordsCollector.class);
	protected DocumentAnalyzer analyzer;

	public AbstractDocumentWordsCollector(Context context) {
		super(context);
		String analyzerClass = context.getConfiguration().get("processor.document.analyzer.class");
		LOG.info("Analyzer class name: class=" + analyzerClass);
		analyzer = ReflectionUtils.getInstance(
				analyzerClass, DocumentAnalyzer.class, new Object[] { context.getConfiguration() });
	}
	
	@Override
	public void fire() {
		super.fire();
		loadVectors();
		for(String label : inputRootDir.list()) {
			File labelDir = new File(inputRootDir, label);
			File[] files = labelDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.getAbsolutePath().endsWith(fileExtensionName);
				}
			});
			for(File file : files) {
				analyze(label, file);
			}
		}
		// output statistics
		stat();
	}
	
	protected abstract void loadVectors();

	protected abstract void analyze(String label, File file);

	private void stat() {
		LOG.info("STAT: totalDocCount=" + context.getMetadata().getTotalDocCount());
		LOG.info("STAT: labelCount=" + context.getMetadata().getLabelCount());
		Iterator<Entry<String, Map<String, Map<String, Term>>>> iter = context.getMetadata().termTableIterator();
		while(iter.hasNext()) {
			Entry<String, Map<String, Map<String, Term>>> entry = iter.next();
			LOG.info("STAT: label=" + entry.getKey() + ", docCount=" + entry.getValue().size());
		}
	}

}