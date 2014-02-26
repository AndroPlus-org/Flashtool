package gui;

import gui.tools.FastBootToolBoxJob;
import gui.tools.WidgetsTool;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class FastbootToolbox extends Dialog {

	protected Object result;
	protected Shell shlFastbootToolbox;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public FastbootToolbox(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		WidgetsTool.setSize(shlFastbootToolbox);
		shlFastbootToolbox.open();
		shlFastbootToolbox.layout();
		Display display = getParent().getDisplay();
		while (!shlFastbootToolbox.isDisposed()) {
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
		shlFastbootToolbox = new Shell(getParent(), getStyle());
		shlFastbootToolbox.setSize(673, 244);
		shlFastbootToolbox.setText("Fastboot Toolbox");
		shlFastbootToolbox.setLayout(new GridLayout(3, false));
		new Label(shlFastbootToolbox, SWT.NONE);
		new Label(shlFastbootToolbox, SWT.NONE);
		new Label(shlFastbootToolbox, SWT.NONE);
		
		Label lblVersion = new Label(shlFastbootToolbox, SWT.NONE);
		lblVersion.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblVersion.setText("バージョン1.0");
		
		Button btnCheckStatus = new Button(shlFastbootToolbox, SWT.NONE);
		btnCheckStatus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doCheckDeviceStatus();
			}
		});
		btnCheckStatus.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnCheckStatus.setText("現在の端末の状態を確認");
		
		Label lblByDooMLoRD = new Label(shlFastbootToolbox, SWT.NONE);
		lblByDooMLoRD.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		lblByDooMLoRD.setText("By DooMLoRD");
		
		Button btnrRebootFBAdb = new Button(shlFastbootToolbox, SWT.NONE);
		btnrRebootFBAdb.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doRebootFastbootViaAdb();
			}
		});
		btnrRebootFBAdb.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnrRebootFBAdb.setText("fastbootモードへ再起動(ADB経由)");
		new Label(shlFastbootToolbox, SWT.NONE);
		
		Button btnRebootFBFB = new Button(shlFastbootToolbox, SWT.NONE);
		btnRebootFBFB.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doRebootBackIntoFastbootMode();
			}
		});
		btnRebootFBFB.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnRebootFBFB.setText("fastbootモードへ再起動(Fastboot経由)");
		
		Button btnHotboot = new Button(shlFastbootToolbox, SWT.NONE);
		btnHotboot.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(shlFastbootToolbox);
		        dlg.setFilterExtensions(new String[]{"*.sin","*.elf","*.img"});
		        dlg.setText("カーネルの選択");
		        String dir = dlg.open();
		        if (dir!=null)
		        	doHotBoot(dir);
			}
		});
		btnHotboot.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnHotboot.setText("HotBootするカーネルを選択");
		
		Button btnFlashSystem = new Button(shlFastbootToolbox, SWT.NONE);
		btnFlashSystem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(shlFastbootToolbox);
		        dlg.setFilterExtensions(new String[]{"*.sin","*.img","*.ext4","*.yaffs2"});
		        dlg.setText("システムの選択");
		        String dir = dlg.open();
		        if (dir!=null)
		        	doFlashSystem(dir);
			}
		});
		btnFlashSystem.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnFlashSystem.setText("書き込むシステムの選択");
		
		Button btnFlashKernel = new Button(shlFastbootToolbox, SWT.NONE);
		btnFlashKernel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(shlFastbootToolbox);
		        dlg.setFilterExtensions(new String[]{"*.sin","*.elf","*.img"});
		        dlg.setText("カーネルの選択");
		        String dir = dlg.open();
		        if (dir!=null)
		        	doFlashKernel(dir);
			}
		});
		btnFlashKernel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnFlashKernel.setText("書き込むカーネルの選択");
		
		Button btnGetVerInfo = new Button(shlFastbootToolbox, SWT.NONE);
		btnGetVerInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doGetFastbootVerInfo();
			}
		});
		btnGetVerInfo.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnGetVerInfo.setText("バージョン情報取得");
		new Label(shlFastbootToolbox, SWT.NONE);
		
		Button btnGetDeviceInfo = new Button(shlFastbootToolbox, SWT.NONE);
		btnGetDeviceInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doGetConnectedDeviceInfo();
			}
		});
		btnGetDeviceInfo.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnGetDeviceInfo.setText("端末情報取得");
		new Label(shlFastbootToolbox, SWT.NONE);
		
		Button btnReboot = new Button(shlFastbootToolbox, SWT.NONE);
		btnReboot.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doFastbootReboot();
			}
		});
		btnReboot.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnReboot.setText("システムへ再起動");
		new Label(shlFastbootToolbox, SWT.NONE);
		new Label(shlFastbootToolbox, SWT.NONE);
		new Label(shlFastbootToolbox, SWT.NONE);
		
		Button btnClose = new Button(shlFastbootToolbox, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlFastbootToolbox.dispose();
			}
		});
		btnClose.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		btnClose.setText("閉じる");

	}

	public void doRebootFastbootViaAdb() {
		FastBootToolBoxJob job = new FastBootToolBoxJob("Reboot fastboot via ADB");
		job.setAction("doRebootFastbootViaAdb");
		job.schedule();
	}
	
	public void doCheckDeviceStatus(){
		FastBootToolBoxJob job = new FastBootToolBoxJob("Check Device Status");
		job.setAction("doCheckDeviceStatus");
		job.schedule();
	}

	public void doGetConnectedDeviceInfo() {
		FastBootToolBoxJob job = new FastBootToolBoxJob("Get Device Infos");
		job.setAction("doGetConnectedDeviceInfo");
		job.schedule();
	}

	public void doGetFastbootVerInfo() {
		FastBootToolBoxJob job = new FastBootToolBoxJob("Get Device Vers Infos");
		job.setAction("doGetFastbootVerInfo");
		job.schedule();
	}
	
	public void doRebootBackIntoFastbootMode() {
		FastBootToolBoxJob job = new FastBootToolBoxJob("Reboot device into fastboot");
		job.setAction("doRebootBackIntoFastbootMode");
		job.schedule();
	}

	public void doFastbootReboot() {
		FastBootToolBoxJob job = new FastBootToolBoxJob("Reboot device");
		job.setAction("doFastbootReboot");
		job.schedule();
	}

	public void doHotBoot(String kernel) {
		FastBootToolBoxJob job = new FastBootToolBoxJob("Hotboot device");
		job.setAction("doHotbootKernel");
		job.setImage(kernel);
		job.schedule();
	}

	public void doFlashKernel(String kernel) {
		FastBootToolBoxJob job = new FastBootToolBoxJob("Flash kernel to device");
		job.setAction("doFlashKernel");
		job.setImage(kernel);
		job.schedule();
	}

	public void doFlashSystem(String system) {
		FastBootToolBoxJob job = new FastBootToolBoxJob("Flash system to device");
		job.setAction("doFlashSystem");
		job.setImage(system);
		job.schedule();
	}

}
