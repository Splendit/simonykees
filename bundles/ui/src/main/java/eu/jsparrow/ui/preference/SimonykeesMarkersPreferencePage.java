package eu.jsparrow.ui.preference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import eu.jsparrow.core.markers.ResolverVisitorsFactory;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.ui.preference.profile.DefaultActiveMarkers;

/**
 * A preference page for activating and deactivating jSparrow Markers.
 * 
 * @since 4.6.0
 *
 */
public class SimonykeesMarkersPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private final Map<String, Button> checkButtons = new HashMap<>();

	@Override
	public void init(IWorkbench workbench) {
		// required by the parent
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setFont(parent.getFont());
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		mainComposite.setLayoutData(gd);
		mainComposite.setLayout(new GridLayout(1, true));

		Group group = new Group(mainComposite, SWT.NONE);
		group.setText(Messages.SimonykeesMarkersPreferencePage_jSparrowMarkersGroupText);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Map<String, RuleDescription> allMarkerDescriptions = ResolverVisitorsFactory.getAllMarkerDescriptions();
		List<String> allActiveMarkers = SimonykeesPreferenceManager.getAllActiveMarkers();
				
		GridData groupLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		groupLayoutData.heightHint = 400;
		ScrolledComposite scrolledComposite = new ScrolledComposite(group, SWT.V_SCROLL);
		scrolledComposite.setLayout(new GridLayout(1, false));
		scrolledComposite.setLayoutData(groupLayoutData);
		
		Composite content = new Composite(scrolledComposite, SWT.NONE);
		content.setLayout(new GridLayout(1, false));
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Tree tree = new Tree(content, SWT.NONE);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));



        // Lists' Purpose: store objects to calculate which ones are empty and needs to be disposed
        List<TreeItem> treeCategoryItems = new ArrayList<TreeItem>();        
        List<Composite> treeCategoryComposites = new ArrayList<Composite>();
        List<TreeEditor> treeCategoryEditors = new ArrayList<TreeEditor>();
        List<Button> treeCategoryButtons = new ArrayList<Button>();
        
        List<ArrayList> treeCategoryMarkerIdsArr = new ArrayList<ArrayList>();
        List<ArrayList> treeItemButtonsArr = new ArrayList<ArrayList>();
        /*  
         * e.g. treeItemButtonsArr is an ArrayList that will store ArrayLists<buttons> for each treeCategory
         * Purpose: if a checkbox for one treeCategory is selected -> only check all buttons within this treeCategory
         */
        
        // Get the names of tags
        List<String> tagNames = new ArrayList<String>();
		for(int i = 0; i < Tag.getAllTags().length; i++) {			
			// Skip the Minimum Java versions
			if(Tag.getAllTags()[i].matches(".*\\d.*") == false){				 //$NON-NLS-1$
				String thisTag = Tag.getAllTags()[i];
				tagNames.add(thisTag);
			}
		}
		
		
		
		
		// Create a <TreeItem> treeCategory for all tags (will generate empty TreeItems)
        for (int i = 0; i < tagNames.size(); i++) {
            TreeItem item = new TreeItem(tree, SWT.NONE);
            item.setText(""); //$NON-NLS-1$
            
            TreeEditor editor = new TreeEditor(tree);
            editor.horizontalAlignment = SWT.LEFT;
            editor.grabHorizontal = true;
            editor.minimumWidth = 16;
            
            Composite composite = new Composite(tree, 0);
            composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
    		GridLayout gridLayout = new GridLayout(2, false);
    		gridLayout.marginHeight = 0;
    		composite.setLayout(gridLayout);

    		Button button = new Button(composite, SWT.CHECK);
    		button.setLayoutData(new GridData(SWT.LEFT, SWT.DEFAULT, false, false));
    		
            Label label = new Label(composite, 0);
            label.setLayoutData(new GridData(SWT.LEFT, SWT.DEFAULT, false, false));
            label.setText( tagNames.get(i)); 
            label.setVisible(true);
            
            editor.setEditor(composite, item);

            treeCategoryItems.add(item);
            treeCategoryComposites.add(composite);
            treeCategoryEditors.add(editor);
            treeCategoryButtons.add(button);
            treeCategoryMarkerIdsArr.add(new ArrayList<String>());
            treeItemButtonsArr.add(new ArrayList<Button>());
       

        }
        
        Menu menu = new Menu(tree);
        tree.setMenu(menu);
        
        // add expand listener
        tree.addTreeListener(new TreeListener() {
			@Override
			public void treeCollapsed(TreeEvent e) {
			}

			@Override
			public void treeExpanded(TreeEvent e) {
				for( TreeItem item : treeCategoryItems ) {
					// collapse all on expand
				   if(item.isDisposed() == false) item.setExpanded(false);
				}
			}
          });
        
		for(Map.Entry<String, RuleDescription> entry : allMarkerDescriptions.entrySet()) {
			String markerId = entry.getKey();
			RuleDescription description = entry.getValue();
			List<Tag> tags = description.getTags();
			
			// Loop all tagNames
	        for(int i = 0; i < tagNames.size(); i++) {
	        	for(int j = 0; j < tags.size(); j++) {
	        		// Put marker into the treeCategory that in name equals its 2nd tag
	        		if(Tag.getTagForName(tagNames.get(i)).toString() == tags.get(j).toString()) {        		
		                TreeItem item = new TreeItem(treeCategoryItems.get(i), SWT.NONE);
		                
		                // add UI through editor
		                TreeEditor editor = new TreeEditor(tree);
		                editor.horizontalAlignment = SWT.LEFT;
		                editor.grabHorizontal = true;
		                editor.minimumWidth = 16;
		                
		        		Composite composite = new Composite(tree, 0);
		        		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		        		GridLayout gridLayout = new GridLayout(4, false);
		        		gridLayout.marginHeight = 0;
		        		composite.setLayout(gridLayout);
		        		
		        		Button button = new Button(composite, SWT.CHECK);
		        		button.setText(description.getName());
		        		button.setLayoutData(new GridData(SWT.LEFT, SWT.DEFAULT, false, false));
		        		
		        		treeCategoryMarkerIdsArr.get(i).add(markerId); // Purpose target markerId when calling SimonykeesPreferenceManager.[]ActiveMarker(markerId);
		        		treeItemButtonsArr.get(i).add(button); // Purpose target this button when setting the button.setSelection()
		                
		                // -- hover UI START
		                // the shaft is missing its arrow
		                Label shaft = new Label(composite, 0);
		                GridData shaftGD = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
		                shaftGD.widthHint = 100;
		                shaft.setLayoutData(shaftGD);
		                shaft.setVisible(false);
		                
		        		shaft.addPaintListener(new PaintListener(){
				        	public void paintControl(PaintEvent e){
				        		e.gc.drawLine(0,8,100,8);
				        		e.gc.dispose();
				        	}
				        });
		        		
		        		String allTags = tags.toString();
		        		Label label = new Label(composite, SWT.NONE);
		        		label.setLayoutData(new GridData(SWT.RIGHT, SWT.DEFAULT, false, false));
		        		label.setText(allTags);
		        		label.setVisible(false);
		        		
		                editor.setEditor(composite, item);
	
		                button.addListener(SWT.MouseExit, new Listener() {
		                	@Override
		                	public void handleEvent(Event event) {
		                		label.setVisible(false);
		                		shaft.setVisible(false);
		                	}
		                });
		                
		                button.addListener(SWT.MouseEnter, new Listener() {
		                	@Override
		                	public void handleEvent(Event event) {
		                		label.setVisible(true);
		                		shaft.setVisible(true);
		                	}
		                });
		                
		             // -- hover UI END
		                
		    			boolean selection = allActiveMarkers.contains(markerId);
		    			button.setSelection(selection);
	
		    			button.addSelectionListener(new SelectionAdapter() {
		    				@Override
		    				public void widgetSelected(SelectionEvent e) {
		    					Button source = (Button) e.getSource();
		    					if (source.getSelection()) {
		    						SimonykeesPreferenceManager.addActiveMarker(markerId);
		    					} else {
		    						SimonykeesPreferenceManager.removeActiveMarker(markerId);
		    					}
		    				}
		    			});
		    		
		    			checkButtons.put(markerId, button);
		        	}
		        }
        	}
		}
	
		// Dispose empty TreeCategories & Add listeners to remaining treeCategoryButtons
		// can only be done once all markers have been printed out
        for(int i = 0; i < treeCategoryItems.size(); i++) {
        	if(treeCategoryItems.get(i).getItems().length == 0) {
        		treeCategoryItems.get(i).dispose();
        		treeCategoryComposites.get(i).dispose();
        		treeCategoryEditors.get(i).dispose();
        	}else {        		
        		int buttonIndex = i;
        		int countCheckedButtonsInCategory = 0;
        		int sizeButtonsInCategory = treeCategoryMarkerIdsArr.get(i).size();
        		
        		// check if every items in a treeCategory -was- selected
        		for(int j = 0; j < sizeButtonsInCategory; j++) {
		    		Button button = (Button) treeItemButtonsArr.get(buttonIndex).get(j);
					Button buttonCategory = (Button)  treeCategoryButtons.get(i);
		    		if(button.getSelection()) countCheckedButtonsInCategory++;
		    		if(countCheckedButtonsInCategory == sizeButtonsInCategory) buttonCategory.setSelection(true);
        		}
        		
        		// add listeners to remaining treeCategoryButtons
        		treeCategoryButtons.get(i).addSelectionListener(new SelectionAdapter() {
        		    @Override
        		    public void widgetSelected(SelectionEvent e) {
        		    	Button source = (Button) e.getSource();
        		    	
        		    	/* If click -> loop how many markers exist in treeCategory
        		    	 * Then (de)select all marker's adjacent buttons
        		    	 */

        		    	for(int j = 0; j < sizeButtonsInCategory; j++) {
        		    		String markerId = treeCategoryMarkerIdsArr.get(buttonIndex).get(j).toString();
        		    		Button button = (Button) treeItemButtonsArr.get(buttonIndex).get(j);
	    					if (source.getSelection()) {
	    						button.setSelection(true);
	    						addActiveMarker(markerId);
	    					} else {
	    						button.setSelection(false);
	    						removeActiveMarker(markerId);
	    					}
	    					checkButtons.put(markerId, button);
        		    	}
        		    }
        		});
        	}
        }
        
        // Method to minimize repetitive lines of code
        // Perhaps switch is the wrong term...
		addSwitchButton(mainComposite, "Deselect All", false, treeCategoryMarkerIdsArr, treeCategoryButtons, treeItemButtonsArr); //$NON-NLS-1$
		addSwitchButton(mainComposite, "Select All", true, treeCategoryMarkerIdsArr, treeCategoryButtons, treeItemButtonsArr); //$NON-NLS-1$
		
		scrolledComposite.setContent(content);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		Point point = content.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrolledComposite.setMinSize(point);

		return mainComposite;
	}
	
	protected void addActiveMarker(String markerId) {
		SimonykeesPreferenceManager.addActiveMarker(markerId);
		
		//Trying to figure out how to select all markers with the same ID
	}
	protected void removeActiveMarker(String markerId) {
		SimonykeesPreferenceManager.removeActiveMarker(markerId);
	}
	
	protected void addSwitchButton(Composite composite, String name, boolean turn, List<ArrayList> markerIdsArr, List<Button> categoryButtons,List<ArrayList> buttonsArr) {
		Button thisButton = new Button(composite, SWT.PUSH);
		thisButton.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
		thisButton.setText(name); 
		thisButton.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// On switch click -> Loop treeCategories (disposed items are still in ArrayList)
				for(int i = 0; i < markerIdsArr.size(); i++) {
					//Skip empty treeCategories
					if(markerIdsArr.get(i).size() != 0) {      
						// Loop markerIds in each treeCategory to (de)select them all
						for(int j = 0; j < markerIdsArr.get(i).size(); j++) {     
							String markerId = markerIdsArr.get(i).get(j).toString();
							Button buttonCategory = (Button) categoryButtons.get(i);
							Button button = (Button) buttonsArr.get(i).get(j);
							buttonCategory.setSelection(turn);
							button.setSelection(turn);
							if(turn){
								SimonykeesPreferenceManager.addActiveMarker(markerId);
							}else {
								SimonykeesPreferenceManager.removeActiveMarker(markerId);
							}
							checkButtons.put(markerId, button);
						}
					}
				}
			}
		});
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		for (String marker : SimonykeesPreferenceManager.getAllActiveMarkers()) {
			SimonykeesPreferenceManager.removeActiveMarker(marker);
			Button button = checkButtons.get(marker);
			button.setSelection(false);
		}
		DefaultActiveMarkers defaultMarkers = new DefaultActiveMarkers();
		for (String marker : defaultMarkers.getActiveMarkers()) {
			SimonykeesPreferenceManager.addActiveMarker(marker);
			Button button = checkButtons.get(marker);
			button.setSelection(true);
		}
	}
}
