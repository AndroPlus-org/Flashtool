package gui;

import java.util.Iterator;

import gui.tools.WidgetTask;
import gui.tools.WidgetsTool;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.system.DeviceEntry;
import org.eclipse.swt.widgets.Combo;

public class AddCustId extends Dialog {

	protected Object result;
	protected Shell shlAddCdfID;
	private Text textID;
	private Combo comboModel;
	private DeviceEntry _entry;
	private Text textName;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public AddCustId(Shell parent, int style) {
		super(parent, style);
		setText("Root Package chooser");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open(DeviceEntry entry) {
		_entry = entry;
		createContents();
		WidgetsTool.setSize(shlAddCdfID);
		
		Label lblNewLabel = new Label(shlAddCdfID, SWT.NONE);
		lblNewLabel.setBounds(10, 44, 111, 15);
		lblNewLabel.setText("ID :");
		
		textID = new Text(shlAddCdfID, SWT.BORDER);
		textID.setBounds(10, 65, 194, 21);
		
		comboModel = new Combo(shlAddCdfID, SWT.NONE);
		comboModel.setBounds(60, 15, 144, 23);
		Iterator imodel =_entry.getVariantList().iterator();
		while (imodel.hasNext()) {
			comboModel.add((String)imodel.next());
		}
		
		Label lblModel = new Label(shlAddCdfID, SWT.NONE);
		lblModel.setBounds(10, 18, 55, 15);
		lblModel.setText("Model :");
		
		Label lblNewLabel_1 = new Label(shlAddCdfID, SWT.NONE);
		lblNewLabel_1.setBounds(10, 92, 55, 15);
		lblNewLabel_1.setText("Name :");
		
		textName = new Text(shlAddCdfID, SWT.BORDER);
		textName.setBounds(10, 113, 194, 21);
		shlAddCdfID.open();
		shlAddCdfID.layout();
		Display display = getParent().getDisplay();
		while (!shlAddCdfID.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlAddCdfID = new Shell(getParent(), getStyle());
		shlAddCdfID.addListener(SWT.Close, new Listener() {
		      public void handleEvent(Event event) {
		    	  result = "";
		    	  event.doit = true;
		      }
		    });
		shlAddCdfID.setSize(237, 209);
		shlAddCdfID.setText("Add CdfID");
		
		Button btnCancel = new Button(shlAddCdfID, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				result = "";
				shlAddCdfID.dispose();
			}
		});
		btnCancel.setBounds(146, 143, 75, 25);
		btnCancel.setText("Cancel");
		
		Button btnOK = new Button(shlAddCdfID, SWT.NONE);
		btnOK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (comboModel.getText().length()==0 || textID.getText().length()==0 || textName.getText().length()==0)
					WidgetTask.openOKBox(shlAddCdfID, "All fields must be set");
				else {
					result = comboModel.getText();
					shlAddCdfID.dispose();
				}
			}
		});
		btnOK.setBounds(60, 143, 75, 25);
		btnOK.setText("Ok");

	}
}
