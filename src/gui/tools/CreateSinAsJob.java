package gui.tools;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.logger.MyLogger;
import org.system.OS;
import org.system.ProcessBuilderWrapper;

public class CreateSinAsJob extends Job {

	boolean canceled = false;
	String file;
	String partition;
	String spareinfo;

	public CreateSinAsJob(String name) {
		super(name);
	}
	
	public void setFile(String f) {
		file=f;
	}
	
	public void setPartition(String part) {
		partition = part;
	}
	
	public void setSpare(String spare) {
		spareinfo = spare;
	}
    
	protected IStatus run(IProgressMonitor monitor) {
        if (file != null) {
			try {
				MyLogger.getLogger().info("sinファイル生成中:"+file+".sin");
				MyLogger.getLogger().info("お待ちください");
				if (spareinfo.equals("09")) {
					ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {OS.getBin2SinPath(),file, partition, "0x"+spareinfo,"0x20000"},false);
				}
				else {
					ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {OS.getBin2SinPath(),file, partition, "0x"+spareinfo,"0x20000", "0x1000"},false);
				}
				MyLogger.getLogger().info("sinファイルを作成しました");
	    		return Status.OK_STATUS;
			}
			catch (Exception ex) {
				MyLogger.getLogger().error(ex.getMessage());
	    		return Status.CANCEL_STATUS;
			}
        }
        else {
        	MyLogger.getLogger().info("sin作成を中止しました (選択したインプットなし");
    		return Status.CANCEL_STATUS;
        }
    }
}
