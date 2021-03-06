/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.developerstudio.eclipse.carbonserver32.configuration;

import java.beans.PropertyChangeListener;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jst.server.generic.core.internal.GenericServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.wso2.developerstudio.eclipse.carbonserver.base.manager.CarbonServerManager;
import org.wso2.developerstudio.eclipse.carbonserver32.util.CarbonServer32Utils;
import org.wso2.developerstudio.eclipse.carbonserver32.util.CarbonServerConstants;
import org.wso2.developerstudio.eclipse.server.base.core.ServerController;

public class ConfigurationPortEditorSection extends ServerEditorSection {

	protected Table ports;
	protected TableViewer viewer;

	/**
	 * ConfigurationPortEditorSection constructor comment.
	 */
	public ConfigurationPortEditorSection() {
		
		super();
	}

	/**
	 * 
	 */
	protected void addChangeListener() {
//		listener = new PropertyChangeListener() {
//			public void propertyChange(PropertyChangeEvent event) {
//				if (TomcatConfiguration.MODIFY_PORT_PROPERTY.equals(event.getPropertyName())) {
//					String id = (String) event.getOldValue();
//					Integer i = (Integer) event.getNewValue();
//					changePortNumber(id, i.intValue());
//				}
//			}
//		};
//		tomcatConfiguration.addPropertyChangeListener(listener);
	}
	
	/**
	 * 
	 * @param name java.lang.String
	 * @param port int
	 */
	protected void changePortNumber(String name, int port) {
		TableItem[] items = ports.getItems();
		int size = items.length;
		for (int i = 0; i < size; i++) {
			ServerPort sp = (ServerPort) items[i].getData();
			if (sp.getName().equals(name)) {
				items[i].setData(new ServerPort(sp.getId(), sp.getName(), port, sp.getProtocol()));
				items[i].setText(1, port + "");
				CarbonServer32Utils.setServerConfigMapValue(server.getOriginal(),sp.getId(),port+"");
				CarbonServer32Utils.updateTransportPorts(server.getOriginal());
				CarbonServer32Utils.updateAxis2XML(server.getOriginal());
				/*if (i == selection) {
					selectPort();
				}*/
				return;
			}
		}
	}
	
	/**
	 * Creates the SWT controls for this workbench part.
	 *
	 * @param parent the parent control
	 */
	public void createSection(Composite parent) {
		super.createSection(parent);
		FormToolkit toolkit = getFormToolkit(parent.getDisplay());
		
		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
			| ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
		section.setText("Ports");
		section.setDescription("Modify the server ports.");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		
		// ports
		Composite composite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 8;
		layout.marginWidth = 8;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
		//IWorkbenchHelpSystem whs = PlatformUI.getWorkbench().getHelpSystem();
		//whs.setHelp(composite, ContextIds.CONFIGURATION_EDITOR_PORTS);
		toolkit.paintBordersFor(composite);
		section.setClient(composite);

		ports = toolkit.createTable(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		ports.setHeaderVisible(true);
		ports.setLinesVisible(true);
		//whs.setHelp(ports, ContextIds.CONFIGURATION_EDITOR_PORTS_LIST);
		
		TableLayout tableLayout = new TableLayout();
	
		TableColumn col = new TableColumn(ports, SWT.NONE);
		col.setText("Port Name");
		ColumnWeightData colData = new ColumnWeightData(15, 150, true);
		tableLayout.addColumnData(colData);

		col = new TableColumn(ports, SWT.NONE);
		col.setText("Value");
		colData = new ColumnWeightData(8, 80, true);
		tableLayout.addColumnData(colData);

		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		data.widthHint = 230;
		data.heightHint = 100;
		ports.setLayoutData(data);
		ports.setLayout(tableLayout);
	
		viewer = new TableViewer(ports);
		viewer.setColumnProperties(new String[] {"name", "port"});

		initialize();
	}

	protected void setupPortEditors() {
		viewer.setCellEditors(new CellEditor[] {null, new TextCellEditor(ports)});
	
		ICellModifier cellModifier = new ICellModifier() {
			public Object getValue(Object element, String property) {
				ServerPort sp = (ServerPort) element;
				if (sp.getPort() < 0)
					return "-";
				return sp.getPort() + "";
			}
	
			public boolean canModify(Object element, String property) {
				if ("port".equals(property))
					return true;
				
				return false;
			}
	
			public void modify(Object element, String property, Object value) {
				try {
					Item item = (Item) element;
					ServerPort sp = (ServerPort) item.getData();
					int port = Integer.parseInt((String) value);
					changePortNumber(sp.getName(),port);
					//execute(new ModifyPortCommand(tomcatConfiguration, sp.getId(), port));
				} catch (Exception ex) {
					// ignore
				}
			}
		};
		viewer.setCellModifier(cellModifier);
		
		// preselect second column (Windows-only)
		String os = System.getProperty("os.name");
		if (os != null && os.toLowerCase().indexOf("win") >= 0) {
			ports.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						int n = ports.getSelectionIndex();
						viewer.editElement(ports.getItem(n).getData(), 1);
					} catch (Exception e) {
						// ignore
					}
				}
			});
		}
	}
	
	public void dispose() {
//		if (tomcatConfiguration != null)
//			tomcatConfiguration.removePropertyChangeListener(listener);
	}
	
	/* (non-Javadoc)
	 * Initializes the editor part with a site and input.
	 */
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		
		GenericServer gserver = (GenericServer) server.getOriginal().getAdapter(GenericServer.class);
		try {
			gserver.getServerInstanceProperties();
			int a=10;
		} catch (Exception e) {
			// ignore
		}
		addChangeListener();
		initialize();
	}

	/**
	 * Initialize the fields in this editor.
	 */
	protected void initialize() {
		if (ports == null)
			return;

		ports.removeAll();
		CarbonServer32Utils.isServerStartBrowserPopup(server.getOriginal());
		ServerPort[] serverPorts = ServerController.getInstance().getServerManager().getServerPorts(server.getOriginal());
		for (ServerPort serverPort : serverPorts) {
			ServerPort port = serverPort;
			TableItem item = new TableItem(ports, SWT.NONE);
			String portStr = "-";
			if (port.getPort() >= 0){
				portStr = port.getPort() + "";
			}
			String[] s = new String[] {port.getName(), portStr};
			item.setText(s);
			//item.setImage(TomcatUIPlugin.getImage(TomcatUIPlugin.IMG_PORT));
			int i = CarbonServerConstants.PORT_CAPTIONS.indexOf(port.getName());
			if (i!=-1){
				port=new ServerPort(CarbonServerConstants.PORT_IDS.get(i), port.getName(), port.getPort(), port.getProtocol());
			}
			item.setData(port);
		}
		
		if (readOnly) {
			viewer.setCellEditors(new CellEditor[] {null, null});
			viewer.setCellModifier(null);
		} else {
			setupPortEditors();
		}
	}
}
