package gui.tools;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.logger.MyLogger;

import flashsystem.X10flash;

public class FlashJob extends Job {

	X10flash flash = null;
	boolean canceled = false;
	Shell _shell;

	public FlashJob(String name) {
		super(name);
	}
	
	public void setFlash(X10flash f) {
		flash=f;
	}
	
	public void setShell(Shell shell) {
		_shell = shell;
	}
	
    protected IStatus run(IProgressMonitor monitor) {
    	try {
    		if (flash.getBundle().open()) {
    			MyLogger.getLogger().info("端末をflashmodeで接続してください。");
    			String result = (String)WidgetTask.openWaitDeviceForFlashmode(_shell,flash);
    			if (result.equals("OK")) {
    				flash.openDevice();
    				flash.flashDevice();
    				flash.getBundle().close();
    			}
    			else {
    				flash.getBundle().close();
    				MyLogger.getLogger().info("キャンセルしました");
    			}
    		}
    		else {
    			MyLogger.getLogger().info("Cannot open bundle. Flash operation canceled");
    		}
			return Status.OK_STATUS;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		return Status.CANCEL_STATUS;
    	}
    }
}
