/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.web.flow.ui.editor.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.dialogs.TypeSelectionDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.web.flow.core.internal.model.Property;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowEditorInput;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowImages;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowUIUtils;
import org.springframework.ide.eclipse.web.flow.ui.editor.model.WebFlowModelLabelDecorator;
import org.springframework.ide.eclipse.web.flow.ui.editor.model.WebFlowModelLabelProvider;

public class ActionPropertiesDialog extends TitleAreaDialog {

    private static int RADIOBEANREF_CHOICE = 0;

    private static int RADIOCLASS_CHOICE = 1;

    private static int RADIOCLASSREF_CHOICE = 2;

    private IAction action;

    private IAction actionClone;

    private Button addButton;

    private Label autowireLabel;

    private Combo autowireText;

    private Label beanLabel;

    private IBeansConfigSet beansConfig;

    private Text beanText;

    private Button browseBeanButton;

    private Button browseClassButton;

    private Button browseClassRefButton;

    private SelectionListener buttonListener = new SelectionAdapter() {

        public void widgetSelected(SelectionEvent e) {
            handleButtonPressed((Button) e.widget);
        }
    };

    private Label classLabel;

    private Label classRefLabel;

    private Text classRefText;

    private Text classText;

    private TableViewer configsViewer;

    private int LABEL_WIDTH = 70;

    private Text methodText;

    private Label nameLabel;

    private Text nameText;

    private Button okButton;

    private IWebFlowModelElement parent;

    private Button radioBeanRef;

    private Button radioClass;

    private Button radioClassRef;

    private Button removeButton;

    private Label viewLabel;

    public ActionPropertiesDialog(Shell parentShell,
            IWebFlowModelElement parent, IAction state) {
        super(parentShell);
        this.action = state;
        this.parent = parent;
        this.actionClone = (IAction) ((ICloneableModelElement) this.action)
                .cloneModelElement();

        WebFlowEditorInput input = WebFlowUIUtils.getActiveFlowEditorInput();
        beansConfig = input.getBeansConfigSet();
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            this.actionClone.setName(trimString(getName()));
            if (this.radioBeanRef.getSelection()) {
                this.actionClone.setBean(trimString(getBean()));
                this.actionClone.setBeanClass(null);
                this.actionClone.setAutowire(null);
                this.actionClone.setClassRef(null);
            }
            else if (this.radioClass.getSelection()) {
                this.actionClone.setBean(null);
                this.actionClone.setBeanClass(trimString(getBeanClass()));
                this.actionClone.setAutowire(trimString(getAutowire()));
                this.actionClone.setClassRef(null);
            }
            else if (this.radioClassRef.getSelection()) {
                this.actionClone.setBean(null);
                this.actionClone.setBeanClass(null);
                this.actionClone.setAutowire(null);
                this.actionClone.setClassRef(trimString(getClassRef()));
            }
            this.actionClone.setMethod(trimString(getMethod()));
            ((ICloneableModelElement) this.action)
                    .applyCloneValues((ICloneableModelElement) this.actionClone);
        }
        super.buttonPressed(buttonId);
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(getShellTitle());
        shell.setImage(getImage());
    }

    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        okButton = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
        // do this here because setting the text will set enablement on the
        // ok button
        nameText.setFocus();
        if (this.action != null && this.action.getName() != null) {
            okButton.setEnabled(true);
        }
        else {
            okButton.setEnabled(false);
        }
    }

    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle(getTitle());
        setMessage(getMessage());
        return contents;
    }

    protected Control createDialogArea(Composite parent) {
        Composite parentComposite = (Composite) super.createDialogArea(parent);
        GridData gridData = null;
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        //layout.verticalSpacing = 10;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite nameGroup = new Composite(composite, SWT.NULL);
        nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout1 = new GridLayout();
        layout1.numColumns = 2;
        layout1.marginWidth = 5;
        //layout1.horizontalSpacing = 10;
        //layout1.verticalSpacing = 10;
        nameGroup.setLayout(layout1);
        nameLabel = new Label(nameGroup, SWT.NONE);
        nameLabel.setText("Name");
        nameText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
        if (this.action != null && this.action.getName() != null) {
            this.nameText.setText(this.action.getName());
        }
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nameText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        //Group for attribute mapper settings.
        Group groupActionType = new Group(composite, SWT.NULL);
        GridLayout layoutAttMap = new GridLayout();
        layoutAttMap.marginWidth = 3;
        layoutAttMap.marginHeight = 3;
        groupActionType.setLayout(layoutAttMap);
        groupActionType.setText(" Action Impementation ");
        groupActionType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite methodComposite = new Composite(groupActionType, SWT.NONE);
        methodComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout3 = new GridLayout();
        layout3.marginHeight = 3;
        layout3.marginWidth = 20;
        layout3.numColumns = 2;
        methodComposite.setLayout(layout3);

        Label methodLabel = new Label(methodComposite, SWT.NONE);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = LABEL_WIDTH;
        methodLabel.setLayoutData(gridData);
        methodLabel.setText("Method");

        methodText = new Text(methodComposite, SWT.SINGLE | SWT.BORDER);
        if (this.action != null && this.action.getMethod() != null) {
            methodText.setText(this.action.getMethod());
        }
        methodText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        methodText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        // Create the radio button for no attribute mapper.
        radioBeanRef = new Button(groupActionType, SWT.RADIO);
        if (this.action != null && this.action.getBean() != null
                && this.action.getBean() != null) {
            radioBeanRef.setSelection(true);
        }
        radioBeanRef.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        radioBeanRef.setText("Locate action by bean reference");
        radioBeanRef.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                setActionImplementationChoice(RADIOBEANREF_CHOICE);
            }
        });
        // Inset composite for classname.
        Composite inset1 = new Composite(groupActionType, SWT.NULL);
        GridLayout inset1Layout = new GridLayout();
        inset1Layout.numColumns = 3;
        inset1Layout.marginWidth = 20;
        inset1Layout.marginHeight = 2;
        inset1.setLayout(inset1Layout);
        inset1.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Label field.
        beanLabel = new Label(inset1, SWT.NONE);
        beanLabel.setText("Bean");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = LABEL_WIDTH;
        beanLabel.setLayoutData(gridData);

        // Add the text box for action classname.
        beanText = new Text(inset1, SWT.SINGLE | SWT.BORDER);
        if (this.action != null && this.action.getBean() != null) {
            beanText.setText(this.action.getBean());
        }
        beanText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        beanText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
        browseBeanButton = new Button(inset1, SWT.PUSH);
        browseBeanButton.setText("...");
        browseBeanButton.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_END));
        browseBeanButton.addSelectionListener(buttonListener);

        radioClass = new Button(groupActionType, SWT.RADIO);
        if (this.action != null && this.action.getBeanClass() != null) {
            radioClass.setSelection(true);
        }
        radioClass.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        radioClass.setText("Locate action by class");
        radioClass.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                setActionImplementationChoice(RADIOCLASS_CHOICE);
            }
        });

        // Inset composite for classname.
        Composite inset2 = new Composite(groupActionType, SWT.NULL);
        GridLayout inset2Layout = new GridLayout();
        inset2Layout.numColumns = 3;
        inset2Layout.marginWidth = 20;
        inset2Layout.marginHeight = 2;
        inset2.setLayout(inset2Layout);
        inset2.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Label field.
        classLabel = new Label(inset2, SWT.NONE);
        classLabel.setText("Class");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = LABEL_WIDTH;
        classLabel.setLayoutData(gridData);

        // Add the text box for action classname.
        classText = new Text(inset2, SWT.SINGLE | SWT.BORDER);
        if (this.action != null && this.action.getBeanClass() != null) {
            classText.setText(this.action.getBeanClass());
        }
        classText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        classText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
        // Add the button for browsing types.
        browseClassButton = new Button(inset2, SWT.PUSH);
        browseClassButton.setText("...");
        browseClassButton.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_END));
        browseClassButton.addSelectionListener(buttonListener);

        //      Label field.
        autowireLabel = new Label(inset2, SWT.NONE);
        autowireLabel.setText("Autowire");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = LABEL_WIDTH;
        autowireLabel.setLayoutData(gridData);

        // Add the text box for action classname.

        autowireText = new Combo(inset2, SWT.READ_ONLY);
        autowireText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        autowireText.setItems(new String[] { "no", "byName", "byType",
                "constructor", "autodetect", "default" });
        if (this.action != null && this.action.getAutowire() != null) {
            autowireText.setText(this.action.getAutowire());
        }
        autowireText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
        radioClassRef = new Button(groupActionType, SWT.RADIO);
        if (this.action != null && this.action.getClassRef() != null) {
            radioClassRef.setSelection(true);
        }
        radioClassRef.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        radioClassRef.setText("Locate attribute mapper by class reference");
        radioClassRef.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                setActionImplementationChoice(RADIOCLASSREF_CHOICE);
            }
        });

        // Inset composite for classname.
        Composite inset3 = new Composite(groupActionType, SWT.NULL);
        GridLayout inset3Layout = new GridLayout();
        inset3Layout.numColumns = 3;
        inset3Layout.marginWidth = 20;
        inset3Layout.marginHeight = 2;
        inset3.setLayout(inset3Layout);
        inset3.setLayoutData(new GridData(GridData.FILL_BOTH));

        //      Label field.
        classRefLabel = new Label(inset3, SWT.NONE);
        classRefLabel.setText("Classref");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = LABEL_WIDTH;
        classRefLabel.setLayoutData(gridData);

        // Add the text box for action classname.
        classRefText = new Text(inset3, SWT.SINGLE | SWT.BORDER);
        if (this.action != null && this.action.getClassRef() != null) {
            classRefText.setText(this.action.getClassRef());
        }
        classRefText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        classRefText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
        // Add the button for browsing types.
        browseClassRefButton = new Button(inset3, SWT.PUSH);
        browseClassRefButton.setText("...");
        browseClassRefButton.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_END));
        browseClassRefButton.addSelectionListener(buttonListener);

        Group groupPropertyType = new Group(composite, SWT.NULL);
        GridLayout layoutPropMap = new GridLayout();
        layoutPropMap.marginWidth = 3;
        layoutPropMap.marginHeight = 3;
        groupPropertyType.setLayout(layoutPropMap);
        groupPropertyType.setText(" Properties ");
        groupPropertyType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite tableAndButtons = new Composite(groupPropertyType, SWT.NONE);
        tableAndButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout2 = new GridLayout();
        layout2.marginHeight = 0;
        layout2.marginWidth = 0;
        layout2.numColumns = 2;
        tableAndButtons.setLayout(layout2);

        Table configsTable = new Table(tableAndButtons, SWT.MULTI
                | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
        GridData data = new GridData(GridData.FILL_BOTH);
        //data.widthHint = 250;
        data.heightHint = 50;
        configsTable.setLayoutData(data);
        TableColumn columnName = new TableColumn(configsTable, SWT.NONE);
        columnName.setText("Name");
        columnName.setWidth(150);
        TableColumn columnValue = new TableColumn(configsTable, SWT.NONE);
        columnValue.setText("Value");
        columnValue.setWidth(220);
        configsTable.setHeaderVisible(true);

        configsViewer = new TableViewer(configsTable);
        String[] columnNames = new String[] { "Name", "Value" };
        configsViewer.setColumnProperties(columnNames);
        configsViewer.setContentProvider(new PropertiesContentProvider(
                this.actionClone, configsViewer));
        CellEditor[] editors = new CellEditor[2];
        TextCellEditor textEditor = new TextCellEditor(configsViewer.getTable());
        TextCellEditor textEditor1 = new TextCellEditor(configsViewer
                .getTable());
        editors[0] = textEditor;
        editors[1] = textEditor1;
        configsViewer.setCellEditors(editors);
        configsViewer.setLabelProvider(new ModelTableLabelProvider());
        configsViewer.setCellModifier(new TableCellModifier());
        configsViewer.setInput(this.actionClone);
        configsTable.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                handleTableSelectionChanged();
            }
        });
        Composite buttonArea = new Composite(tableAndButtons, SWT.NONE);
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        buttonArea.setLayout(layout);
        buttonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        addButton = new Button(buttonArea, SWT.PUSH);
        addButton.setText("Add");
        GridData data1 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        data1.widthHint = 40;
        addButton.setLayoutData(data1);
        addButton.addSelectionListener(new SelectionAdapter() {

            // Add a task to the ExampleTaskList and refresh the view
            public void widgetSelected(SelectionEvent e) {
                new Property(actionClone, "name", "value");
            }
        });

        removeButton = new Button(buttonArea, SWT.PUSH);
        removeButton.setText("Delete");
        GridData data2 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        data1.widthHint = 40;
        removeButton.setLayoutData(data2);
        removeButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) configsViewer
                        .getSelection();
                if (selection.getFirstElement() != null) {
                    if (selection.getFirstElement() instanceof IProperty) {
                        actionClone.removeProperty((IProperty) selection
                                .getFirstElement());
                    }
                }
            }
        });
        removeButton.setEnabled(false);

        applyDialogFont(parentComposite);
        setActionImplementationEnabled();
        return parentComposite;
    }

    /**
     * @return Returns the autowire.
     */
    public String getAutowire() {
        return this.autowireText.getText();
    }

    /**
     * @return Returns the bean.
     */
    public String getBean() {
        return this.beanText.getText();
    }

    /**
     * @return Returns the beanClass.
     */
    public String getBeanClass() {
        return this.classText.getText();
    }

    /**
     * @return Returns the classRef.
     */
    public String getClassRef() {
        return this.classRefText.getText();
    }

    protected Image getImage() {
        return WebFlowImages.getImage(WebFlowImages.IMG_OBJS_ACTION);
    }

    protected String getMessage() {
        return "Enter the details for the action";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IAction#getMethod()
     */
    public String getMethod() {
        return this.methodText.getText();
    }

    public String getName() {
        return this.nameText.getText();
    }

    protected String getShellTitle() {
        return "Action";
    }

    protected String getTitle() {
        return "Action properties";
    }

    /**
     * One of the buttons has been pressed, act accordingly.
     */
    private void handleButtonPressed(Button button) {

        if (button.equals(browseBeanButton)) {
            WebFlowEditorInput input = WebFlowUIUtils.getActiveFlowEditorInput();
            IBeansConfigSet beansConfig = input.getBeansConfigSet();
            ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                    getShell(), new DecoratingLabelProvider(
                            new WebFlowModelLabelProvider(),
                            new WebFlowModelLabelDecorator()));
            dialog.setBlockOnOpen(true);
            List elements = new ArrayList();
            Iterator iter = beansConfig.getConfigs().iterator();
            IBeansProject parent = (IBeansProject) beansConfig
                    .getElementParent();
            while (iter.hasNext()) {
                String config = (String) iter.next();
                elements.addAll(parent.getConfig(config).getBeans());
            }
            dialog.setSize(100, 20);
            dialog.setElements(elements.toArray());
            dialog.setEmptySelectionMessage("Select a bean reference");
            dialog.setTitle("Bean reference");
            dialog.setMessage("Please select a bean reference");
            dialog.setMultipleSelection(false);
            if (Dialog.OK == dialog.open()) {
                this.beanText.setText(((IBean) dialog.getFirstResult())
                        .getElementName());
                this.setActionImplementationChoice(RADIOBEANREF_CHOICE);
            }

        }
        else {
            IProject project = this.parent.getElementResource().getProject();
            IJavaSearchScope searchScope = SearchEngine.createWorkspaceScope();
            try {
                if (project.hasNature(JavaCore.NATURE_ID)) {
                    IJavaProject javaProject = (IJavaProject) project
                            .getNature(JavaCore.NATURE_ID);
                    IType type = javaProject
                            .findType("org.springframework.web.flow.Action");
                    if (type != null) {
                        searchScope = SearchEngine.createHierarchyScope(type);
                    }
                }
            }
            catch (JavaModelException e) {
            }
            catch (CoreException e) {
            }
            /*TypeSelectionDialog2 dialog= new TypeSelectionDialog2(getShell(), false, 
                    new ProgressMonitorDialog(getShell()), searchScope,  IJavaSearchConstants.TYPE);
            dialog.setMessage(JavaUIMessages.JavaUI_defaultDialogMessage);*/ 
            
            TypeSelectionDialog dialog = new TypeSelectionDialog(getShell(),
                    new ProgressMonitorDialog(getShell()),
                    IJavaSearchConstants.CLASS, searchScope);
            dialog.setMessage("Select an action implementation class"); //$NON-NLS-1$
            dialog.setBlockOnOpen(true);
            dialog.setTitle("Action Class");
            //dialog.setFilter("*");
            if (Dialog.OK == dialog.open()) {
                IType obj = (IType) dialog.getFirstResult();
                if (button.equals(browseClassButton)) {
                    this.classText.setText(obj.getFullyQualifiedName());
                    this.setActionImplementationChoice(RADIOCLASS_CHOICE);
                }
                else if (button.equals(browseClassRefButton)) {
                    this.classRefText.setText(obj.getFullyQualifiedName());
                    this.setActionImplementationChoice(RADIOCLASSREF_CHOICE);
                }
            }
        }
        this.validateInput();

    }

    /**
     * The user has selected a different configuration in table. Update button
     * enablement.
     */
    private void handleTableSelectionChanged() {
        IStructuredSelection selection = (IStructuredSelection) configsViewer
                .getSelection();
        if (selection.isEmpty()) {
            removeButton.setEnabled(false);
        }
        else {
            removeButton.setEnabled(true);
        }
    }

    protected void setActionImplementationChoice(int choice) {
        if (RADIOBEANREF_CHOICE == choice) {
            this.radioBeanRef.setSelection(true);
            this.radioClass.setSelection(false);
            this.radioClassRef.setSelection(false);

            this.beanText.setEnabled(true);
            this.browseBeanButton.setEnabled(true);
            this.beanLabel.setEnabled(true);

            this.classText.setEnabled(false);
            this.browseClassButton.setEnabled(false);
            this.autowireText.setEnabled(false);
            this.classLabel.setEnabled(false);
            this.autowireLabel.setEnabled(false);

            this.classRefText.setEnabled(false);
            this.browseClassRefButton.setEnabled(false);
            this.classRefLabel.setEnabled(false);
        }
        else if (RADIOCLASS_CHOICE == choice) {
            this.radioBeanRef.setSelection(false);
            this.radioClass.setSelection(true);
            this.radioClassRef.setSelection(false);

            this.beanText.setEnabled(false);
            this.browseBeanButton.setEnabled(false);
            this.beanLabel.setEnabled(false);

            this.classText.setEnabled(true);
            this.browseClassButton.setEnabled(true);
            this.autowireText.setEnabled(true);
            this.classLabel.setEnabled(true);
            this.autowireLabel.setEnabled(true);

            this.classRefText.setEnabled(false);
            this.browseClassRefButton.setEnabled(false);
            this.classRefLabel.setEnabled(false);
        }
        else if (RADIOCLASSREF_CHOICE == choice) {
            this.radioBeanRef.setSelection(false);
            this.radioClass.setSelection(false);
            this.radioClassRef.setSelection(true);

            this.beanText.setEnabled(false);
            this.browseBeanButton.setEnabled(false);
            this.beanLabel.setEnabled(false);

            this.classText.setEnabled(false);
            this.browseClassButton.setEnabled(false);
            this.autowireText.setEnabled(false);
            this.classLabel.setEnabled(false);
            this.autowireLabel.setEnabled(false);

            this.classRefText.setEnabled(true);
            this.browseClassRefButton.setEnabled(true);
            this.classRefLabel.setEnabled(true);
        }
        else {
            this.radioBeanRef.setSelection(false);
            this.radioClass.setSelection(false);
            this.radioClassRef.setSelection(false);

            this.beanText.setEnabled(false);
            this.browseBeanButton.setEnabled(false);
            this.beanLabel.setEnabled(false);

            this.classText.setEnabled(false);
            this.browseClassButton.setEnabled(false);
            this.autowireText.setEnabled(false);
            this.classLabel.setEnabled(false);
            this.autowireLabel.setEnabled(false);

            this.classRefText.setEnabled(false);
            this.browseClassRefButton.setEnabled(false);
            this.classRefLabel.setEnabled(false);
        }

        if (this.beansConfig == null) {
            this.browseBeanButton.setEnabled(false);
            this.browseClassButton.setEnabled(false);
            this.browseClassRefButton.setEnabled(false);
        }
    }

    protected void setActionImplementationEnabled() {
        this.radioBeanRef.setEnabled(true);
        this.radioClass.setEnabled(true);
        this.radioClassRef.setEnabled(true);

        if (this.action.getBean() != null) {
            this.setActionImplementationChoice(RADIOBEANREF_CHOICE);
        }
        else if (this.action.getBeanClass() != null) {
            this.setActionImplementationChoice(RADIOCLASS_CHOICE);
        }
        else if (this.action.getClassRef() != null) {
            this.setActionImplementationChoice(RADIOCLASSREF_CHOICE);
        }
    }

    protected void showError(String error) {
        super.setErrorMessage(error);
    }

    public String trimString(String string) {
        if (string != null && string == "") {
            string = null;
        }
        return string;
    }

    protected void validateInput() {
        String bean = this.beanText.getText();
        String clazz = this.classText.getText();
        String autowire = this.autowireText.getText();
        String classRef = this.classRefText.getText();
        boolean error = false;
        StringBuffer errorMessage = new StringBuffer();
        if (this.radioBeanRef.getSelection()
                && (bean == null || "".equals(bean))) {
            errorMessage
                    .append("A valid bean reference attribute is required. ");
            error = true;
        }
        if (this.radioClass.getSelection()
                && (clazz == null || "".equals(clazz) || clazz.indexOf(".") == -1)) {
            errorMessage.append("A valid bean class name is required. ");
            error = true;
        }
        if (this.radioClass.getSelection()
                && (autowire == null || "".equals(autowire))) {
            errorMessage.append("Please select an autowire type. ");
            error = true;
        }
        if (this.radioClassRef.getSelection()
                && (classRef == null || "".equals(classRef) || classRef
                        .indexOf(".") == -1)) {
            errorMessage.append("A valid bean class reference is required. ");
            error = true;
        }
        if (error) {
            getButton(OK).setEnabled(false);
            setErrorMessage(errorMessage.toString());
        }
        else {
            getButton(OK).setEnabled(true);
            setErrorMessage(null);
        }
    }
}