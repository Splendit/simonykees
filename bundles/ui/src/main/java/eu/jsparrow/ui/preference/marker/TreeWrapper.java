package eu.jsparrow.ui.preference.marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class TreeWrapper {
	private Tree tree;
	private Composite parent;
	private List<Category> categories = new ArrayList<>();

	public TreeWrapper(Composite parent) {
		this.parent = parent;
	}

	public void init(List<String> allActiveMarkers,
			Map<String, RuleDescription> allMarkerDescriptions) {
		tree = new Tree(parent, SWT.NONE);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		List<String> tags = Arrays.stream(Tag.getAllTags())
			.filter(StringUtils::isAlpha)
			.filter(tag -> !"free".equalsIgnoreCase(tag)) //$NON-NLS-1$
			.sorted()
			.collect(Collectors.toList());
		for (String tag : tags) {
			Category category = new Category(this, tag, allActiveMarkers);
			category.initCategory();
			categories.add(category);
		}
		
		List<TreeItem> treeCategoryItems = categories.stream()
				.map(Category::getTreeItem)
				.collect(Collectors.toList());
		
        Menu menu = new Menu(tree);
        tree.setMenu(menu);
        
        // add expand listener
        tree.addTreeListener(new TreeListener() {
			@Override
			public void treeCollapsed(TreeEvent e) {
				/*
				 * No action taken on collapse
				 */
			}

			@Override
			public void treeExpanded(TreeEvent e) {

				for(TreeItem item : treeCategoryItems) {
					// collapse all on expand
				   if(!item.isDisposed()) item.setExpanded(false);
				}
			}
          });
        
		for (Category category : categories) {
			Map<String, RuleDescription> categoryMarkerDescriptions = findByTag(allMarkerDescriptions, category.getTag());
			category.initCategoryEntries(categoryMarkerDescriptions);
		}

	}

	private Map<String, RuleDescription> findByTag(Map<String, RuleDescription> allMarkerDescriptions, String tag) {
		Map<String, RuleDescription> map = new HashMap<>();
		for (Map.Entry<String, RuleDescription> entry : allMarkerDescriptions.entrySet()) {
			RuleDescription description = entry.getValue();
			for (Tag ruleTag : description.getTags()) {
				if (Tag.getTagForName(tag) == ruleTag) {
					map.put(entry.getKey(), description);
				}
			}
		}
		return map;
	}

	public Tree getTree() {
		return tree;
	}

	public void setSelectionByMarkerId(String markerId, boolean selection) {
		for (Category category : categories) {
			category.setSelectionByMarker(markerId, selection);
		}
	}
	
	public void setEnabledByMarkerId(String markerId, boolean selection) {
		for (Category category : categories) {
			category.setEnabledByMarker(markerId, selection);
		}
	}
	
	public void bulkUpdateAllCategories(boolean selection) {
		for(Category category : categories) {
			category.setCategorySelection(selection);
		}
	}
}
