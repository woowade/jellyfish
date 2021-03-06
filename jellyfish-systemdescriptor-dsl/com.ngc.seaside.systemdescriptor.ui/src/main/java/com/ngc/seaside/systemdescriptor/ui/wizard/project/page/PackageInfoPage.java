/**
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.ngc.seaside.systemdescriptor.ui.wizard.project.page;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * This class enables the user to Enter the default project and default file name
 * for the System Descriptor project.
 */
public class PackageInfoPage extends WizardPage {

   private static final String DEFAULT_PACKAGE = "com.ngc.mysdproject";
   private static final String DEFAULT_FILE = "MyModel";

   private Text pkgTextField;
   private Text filenameTextField;
   private Button createDefaultPkgCheck;
   private Button createDefaultFileCheck;
   private Composite container;

   private KeyAdapter keyListener = new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
         updateControls();
      }
   };

   private SelectionListener selectionListener = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
         updateControls();
      }
   };


   /**
    * Constructor for the page that asks the user for default package and model file information.
    */
   public PackageInfoPage() {
      super("Package Info Page");
      setTitle("Package Info Page");
      setDescription("Enter package information");

      setControl(pkgTextField);
   }

   @Override
   public void createControl(Composite parent) {
      container = new Composite(parent, SWT.NONE);
      container.setLayout(new GridLayout(2, false));

      buildPackageInfo();
      buildFileInfo();

      // required to avoid an error in the system
      setControl(container);

      updateControls();
   }

   /**
    * Get the user's selected preference to determine if the package should be created.
    *
    * @return true if the default package should be created.
    */
   public boolean getCreatePkg() {
      return createDefaultPkgCheck.getSelection();
   }

   /**
    * Get the value of the package. The value will be the fully qualified package using dot notation.
    *
    * @return the string representation of the package.
    * @see #getCreatePkg()
    */
   public String getPkg() {
      return pkgTextField.getText();
   }

   /**
    * Create the user's selected preference to determine if the default .sd file should be created.
    *
    * @return true if the default file should be created.
    */
   public boolean getCreateFile() {
      return createDefaultFileCheck.getSelection();
   }

   /**
    * Get the name of the default .sd file.
    *
    * @return the file (without the .sd extension)
    * @see #getCreateFile()
    */
   public String getFileName() {
      return filenameTextField.getText();
   }

   /**
    * Builds and populates the default package info GUI components into the page.
    */
   private void buildPackageInfo() {
      GridData gd = new GridData(GridData.FILL_HORIZONTAL);

      createDefaultPkgCheck = new Button(container, SWT.CHECK);
      createDefaultPkgCheck.setText("Create default package");
      createDefaultPkgCheck.setSelection(true);
      createDefaultPkgCheck.addSelectionListener(selectionListener);

      takeUpCellInGrid(container);

      Label labelPkg = new Label(container, SWT.NONE);
      labelPkg.setText("Package name:");

      pkgTextField = new Text(container, SWT.BORDER | SWT.SINGLE);
      pkgTextField.setText(DEFAULT_PACKAGE);
      pkgTextField.setEnabled(true);
      pkgTextField.addKeyListener(keyListener);
      pkgTextField.setLayoutData(gd);

      takeUpCellInGrid(container);
      takeUpCellInGrid(container);
   }

   /**
    * Builds and populates the default model file info GUI components into the page.
    */
   private void buildFileInfo() {
      GridData gd = new GridData(GridData.FILL_HORIZONTAL);

      createDefaultFileCheck = new Button(container, SWT.CHECK);
      createDefaultFileCheck.setText("Create default file in package");
      createDefaultFileCheck.setSelection(true);
      createDefaultFileCheck.addSelectionListener(selectionListener);

      takeUpCellInGrid(container);

      Label labelPkg = new Label(container, SWT.NONE);
      labelPkg.setText("File name:");

      filenameTextField = new Text(container, SWT.BORDER | SWT.SINGLE);
      filenameTextField.setText(DEFAULT_FILE);
      filenameTextField.setEnabled(true);
      filenameTextField.addKeyListener(keyListener);
      filenameTextField.setLayoutData(gd);

      takeUpCellInGrid(container);
      takeUpCellInGrid(container);
   }

   /**
    * Adds an empty Label to the next cell in the container which uses a Grid layout.
    * This is just to take up a cell.
    *
    * @param container The container to add the empty label to.
    */
   private void takeUpCellInGrid(Composite container) {
      new Label(container, SWT.NONE);
   }

   /**
    * Updates the GUI controls' status based on inputs.
    */
   private void updateControls() {
      final boolean createDefaultPkg = createDefaultPkgCheck.getSelection();
      final boolean createDefaultFile = createDefaultFileCheck.getSelection();
      final String pkgText = pkgTextField.getText();
      final String fileText = filenameTextField.getText();

      pkgTextField.setEnabled(createDefaultPkg);
      createDefaultFileCheck.setEnabled(createDefaultPkg);

      filenameTextField.setEnabled(createDefaultFile && createDefaultPkg);

      final boolean complete;
      if (createDefaultPkg) {
         if (pkgText.isEmpty()) {
            complete = false;
         } else if (createDefaultFile) {
            if (fileText.isEmpty()) {
               complete = false;
            } else {
               complete = true;
            }
         } else {
            complete = true;
         }
      } else {
         complete = true;
      }

      setPageComplete(complete);
   }

}
