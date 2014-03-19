package gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.Deflater;

import linuxlib.JUsb;

import org.adb.AdbUtility;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.logger.MyLogger;
import org.system.AdbPhoneThread;
import org.system.Device;
import org.system.DeviceChangedListener;
import org.system.DeviceEntry;
import org.system.DeviceProperties;
import org.system.Devices;
import org.system.FTDEntry;
import org.system.FTShell;
import org.system.GlobalConfig;
import org.system.OS;
import org.system.StatusEvent;
import org.system.StatusListener;
import org.system.UpdateURL;

import flashsystem.Bundle;
import flashsystem.SinFile;
import flashsystem.X10flash;
import gui.tools.APKInstallJob;
import gui.tools.BackupSystemJob;
import gui.tools.BackupTAJob;
import gui.tools.BusyboxInstallJob;
import gui.tools.CleanJob;
import gui.tools.DecryptJob;
import gui.tools.DeviceApps;
import gui.tools.FTDExplodeJob;
import gui.tools.FlashJob;
import gui.tools.GetULCodeJob;
import gui.tools.MsgBox;
import gui.tools.OldUnlockJob;
import gui.tools.RawTAJob;
import gui.tools.RootJob;
import gui.tools.VersionCheckerJob;
import gui.tools.WidgetTask;
import gui.tools.WidgetsTool;
import gui.tools.Yaffs2Job;

import org.eclipse.swt.custom.ScrolledComposite;

public class MainSWT {

	protected Shell shlSonyericsson;
	private static AdbPhoneThread phoneWatchdog;
	public static boolean guimode=false;
	protected ToolItem tltmFlash;
	protected ToolItem tltmRoot;
	protected ToolItem tltmAskRoot;
	protected ToolItem tltmBLU;
	protected ToolItem tltmClean;
	protected ToolItem tltmRecovery;
	protected ToolItem tltmApkInstall;
	protected MenuItem mntmSwitchPro;
	protected MenuItem mntmAdvanced;
	protected MenuItem mntmNoDevice;
	protected MenuItem mntmInstallBusybox;
	protected MenuItem mntmRawBackup;
	protected MenuItem mntmRawRestore;
	protected VersionCheckerJob vcheck;
	
	/**
	 * Open the window.
	 */
	public void open() {
		Display.setAppName("Flashtool");
		Display display = Display.getDefault();
		createContents();
		WidgetsTool.setSize(shlSonyericsson);
		guimode=true;
		StatusListener phoneStatus = new StatusListener() {
			public void statusChanged(StatusEvent e) {
				if (!e.isDriverOk()) {
					MyLogger.getLogger().error("接続した端末のドライバがインストールされていません");
					MyLogger.getLogger().error("Flashtoolのフォルダにあるドライバをインストールしてください");
				}
				else {
					if (e.getNew().equals("adb")) {
						MyLogger.getLogger().info("USBデバッグが有効な端末が接続されました");
						MyLogger.getLogger().debug("識別して続行します");
						doIdent();
					}
					if (e.getNew().equals("none")) {
						MyLogger.getLogger().info("端末の接続が解除されています");
						doDisableIdent();
					}
					if (e.getNew().equals("flash")) {
						MyLogger.getLogger().info("端末をflash modeで接続しました");
						doDisableIdent();
					}
					if (e.getNew().equals("fastboot")) {
						MyLogger.getLogger().info("端末をfastboot modeで接続しました");
						doDisableIdent();
					}
					if (e.getNew().equals("normal")) {
						MyLogger.getLogger().info("端末が接続されましたがUSBデバッグが無効です");
						MyLogger.getLogger().info("2011年モデルではMTPモードで接続しないでください");
						doDisableIdent();
					}
				}
			}
		};
		killAdbandFastboot();
		phoneWatchdog = new AdbPhoneThread();
		phoneWatchdog.start();
		phoneWatchdog.addStatusListener(phoneStatus);
		shlSonyericsson.open();
		shlSonyericsson.layout();
		while (!shlSonyericsson.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public void doDisableIdent() {
		WidgetTask.setEnabled(tltmFlash,true);
		WidgetTask.setEnabled(tltmRoot,false);
		WidgetTask.setEnabled(tltmAskRoot,false);
		WidgetTask.setEnabled(tltmApkInstall,false);
		WidgetTask.setMenuName(mntmNoDevice, "端末なし");
		WidgetTask.setEnabled(mntmNoDevice,false);
		WidgetTask.setEnabled(mntmRawRestore,false);
		WidgetTask.setEnabled(mntmRawBackup,false);
		WidgetTask.setEnabled(tltmClean,false);
		WidgetTask.setEnabled(tltmRecovery,false);
	}
	
	/**
	 * Create contents of the window.
	 * @wbp.parser.entryPoint
	 */
	protected void createContents() {
		shlSonyericsson = new Shell();
		shlSonyericsson.addListener(SWT.Close, new Listener() {
		      public void handleEvent(Event event) {
		    	  exitProgram();
		    	  shlSonyericsson.dispose();
		      }
		    });

		shlSonyericsson.setSize(794, 451);
		shlSonyericsson.setText("Sony Mobile Flasher by Androxyde");
		shlSonyericsson.setImage(SWTResourceManager.getImage(MainSWT.class, "/gui/ressources/icons/flash_32.png"));
		shlSonyericsson.setLayout(new FormLayout());
		
		Menu menu = new Menu(shlSonyericsson, SWT.BAR);
		shlSonyericsson.setMenuBar(menu);
		
		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("ファイル");
		
		Menu menu_1 = new Menu(mntmFile);
		mntmFile.setMenu(menu_1);
		
		mntmSwitchPro = new MenuItem(menu_1, SWT.NONE);
		mntmSwitchPro.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean ispro = GlobalConfig.getProperty("devfeatures").equals("yes");
    			GlobalConfig.setProperty("devfeatures", ispro?"no":"yes");
    			mntmAdvanced.setEnabled(!ispro);
    			mntmSwitchPro.setText(!ispro?"シンプルに切り替え":"Proに切り替え");
    			//mnDev.setVisible(!ispro);
    			//mntmSwitchPro.setText(Language.getMessage(mntmSwitchPro.getName()));
    		    //mnDev.setText(Language.getMessage(mnDev.getName()));
			}
		});
		mntmSwitchPro.setText(GlobalConfig.getProperty("devfeatures").equals("yes")?"シンプルに切り替え":"Proに切り替え");
		
		MenuItem mntmExit = new MenuItem(menu_1, SWT.NONE);
		mntmExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exitProgram();
				shlSonyericsson.dispose();
			}
		});
		mntmExit.setText("終了");
		
		mntmNoDevice = new MenuItem(menu, SWT.CASCADE);
		mntmNoDevice.setText("端末なし");
		mntmNoDevice.setEnabled(false);
		
		Menu menu_8 = new Menu(mntmNoDevice);
		mntmNoDevice.setMenu(menu_8);
		
		MenuItem mntmRoot = new MenuItem(menu_8, SWT.CASCADE);
		mntmRoot.setText("Root化");
		
		Menu menu_10 = new Menu(mntmRoot);
		mntmRoot.setMenu(menu_10);
		
		MenuItem mntmForcePsneuter = new MenuItem(menu_10, SWT.NONE);
		mntmForcePsneuter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doRoot("doRootpsneuter");
			}
		});
		mntmForcePsneuter.setText("Force PsNeuter");
		
		MenuItem mntmForceZergrush = new MenuItem(menu_10, SWT.NONE);
		mntmForceZergrush.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doRoot("doRootzergRush");
			}
		});
		mntmForceZergrush.setText("Force zergRush");
		
		MenuItem mntmForceEmulator = new MenuItem(menu_10, SWT.NONE);
		mntmForceEmulator.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doRoot("doRootEmulator");
			}
		});
		mntmForceEmulator.setText("Force Emulator");
		
		MenuItem mntmForceAdbrestore = new MenuItem(menu_10, SWT.NONE);
		mntmForceAdbrestore.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doRoot("doRootAdbRestore");
			}
		});
		mntmForceAdbrestore.setText("Force AdbRestore");
		
		MenuItem mntmForceServicemenu = new MenuItem(menu_10, SWT.NONE);
		mntmForceServicemenu.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doRoot("doRootServiceMenu");
			}
		});
		mntmForceServicemenu.setText("Force ServiceMenu");
		
		MenuItem mntmRunRootShell = new MenuItem(menu_10, SWT.NONE);
		mntmRunRootShell.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doRoot("doRootRunRootShell");
			}
		});
		mntmRunRootShell.setText("Force Run Root Shell");
		
		MenuItem mntmBackupSystemApps = new MenuItem(menu_8, SWT.NONE);
		mntmBackupSystemApps.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				BackupSystemJob bsj = new BackupSystemJob("Backup System apps");
				bsj.schedule();
			}
		});
		mntmBackupSystemApps.setText("システムアプリをバックアップ");
		
		mntmInstallBusybox = new MenuItem(menu_8, SWT.NONE);
		mntmInstallBusybox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
        		String busybox = Devices.getCurrent().getBusybox(true);
        		if (busybox.length()>0) {
        			BusyboxInstallJob bij = new BusyboxInstallJob("Busybox Install");
        			bij.setBusybox(busybox);
        			bij.schedule();
        		}
			}
		});
		mntmInstallBusybox.setText("busyboxをインストール");
		
		MenuItem mntmReboot = new MenuItem(menu_8, SWT.NONE);
		mntmReboot.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					Devices.getCurrent().reboot();
				}
				catch (Exception ex) {
				}
			}
		});
		mntmReboot.setText("再起動");
		
		MenuItem mntmNewSubmenu = new MenuItem(menu, SWT.CASCADE);
		mntmNewSubmenu.setText("ツール");
		
		Menu menu_4 = new Menu(mntmNewSubmenu);
		mntmNewSubmenu.setMenu(menu_4);
		
		MenuItem mntmNewItem = new MenuItem(menu_4, SWT.NONE);
		mntmNewItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SinEditor sedit = new SinEditor(shlSonyericsson,SWT.PRIMARY_MODAL | SWT.SHEET);
				sedit.open();
			}
		});
		mntmNewItem.setText("Sinエディタ");
		
		MenuItem mntmExtractors = new MenuItem(menu_4, SWT.CASCADE);
		mntmExtractors.setText("抽出");
		
		Menu menu_5 = new Menu(mntmExtractors);
		mntmExtractors.setMenu(menu_5);
		
		MenuItem mntmYaffs = new MenuItem(menu_5, SWT.NONE);
		mntmYaffs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doYaffs2Unpack();
			}
		});
		mntmYaffs.setText("Yaffs2");
		
		MenuItem mntmElf = new MenuItem(menu_5, SWT.NONE);
		mntmElf.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ElfEditor elfedit = new ElfEditor(shlSonyericsson,SWT.PRIMARY_MODAL | SWT.SHEET);
				elfedit.open();
			}
		});
		mntmElf.setText("Elf");
		
		MenuItem mntmNewItem_1 = new MenuItem(menu_4, SWT.NONE);
		mntmNewItem_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Decrypt decrypt = new Decrypt(shlSonyericsson,SWT.PRIMARY_MODAL | SWT.SHEET);
				Vector result = decrypt.open();
				if (result!=null) {
					File f = (File)result.get(0);
					final String folder = f.getParent();
					DecryptJob dec = new DecryptJob("Decrypt");
					dec.addJobChangeListener(new IJobChangeListener() {
						public void aboutToRun(IJobChangeEvent event) {
						}

						public void awake(IJobChangeEvent event) {
						}

						public void done(IJobChangeEvent event) {
							String result = WidgetTask.openBundleCreator(shlSonyericsson,folder);
							if (result.equals("キャンセル"))
								MyLogger.getLogger().info("FTF作成をキャンセルしました");
						}

						public void running(IJobChangeEvent event) {
						}

						public void scheduled(IJobChangeEvent event) {
						}

						public void sleeping(IJobChangeEvent event) {
						}
					});

					dec.setFiles(result);
					dec.schedule();
				}
				else {
					MyLogger.getLogger().info("復号を中止しました");
				}
			}
		});
		mntmNewItem_1.setText("SEUS復号");
		
		MenuItem mntmBundleCreation = new MenuItem(menu_4, SWT.NONE);
		mntmBundleCreation.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				BundleCreator cre = new BundleCreator(shlSonyericsson,SWT.PRIMARY_MODAL | SWT.SHEET);
				String result = (String)cre.open();
				if (result.equals("キャンセル"))
					MyLogger.getLogger().info("FTF作成を中止しました");
			}
		});
		mntmBundleCreation.setText("FTF作成");
		
		mntmAdvanced = new MenuItem(menu, SWT.CASCADE);
		mntmAdvanced.setText("高度なオプション");
		
		
		Menu AdvancedMenu = new Menu(mntmAdvanced);
		
		mntmAdvanced.setMenu(AdvancedMenu);
		
		MenuItem mntmTrimArea = new MenuItem(AdvancedMenu, SWT.CASCADE);
		mntmTrimArea.setText("Trim Area");
		
		Menu menu_9 = new Menu(mntmTrimArea);
		mntmTrimArea.setMenu(menu_9);
		
		MenuItem mntmBackup = new MenuItem(menu_9, SWT.NONE);
		mntmBackup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doBackupTA();
			}
		});
		mntmBackup.setText("S1 Dump");
		
		mntmRawBackup = new MenuItem(menu_9, SWT.NONE);
		mntmRawBackup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RawTAJob rj = new RawTAJob("Raw TA");
				rj.setAction("doBackup");
				rj.setShell(shlSonyericsson);
				rj.schedule();
			}
		});
		mntmRawBackup.setText("バックアップ");
		mntmRawBackup.setEnabled(false);
		
		mntmRawRestore = new MenuItem(menu_9, SWT.NONE);
		mntmRawRestore.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RawTAJob rj = new RawTAJob("Raw TA");
				rj.setAction("doRestore");
				rj.setShell(shlSonyericsson);
				rj.schedule();
			}
		});
		mntmRawRestore.setText("リストア");
		mntmRawRestore.setEnabled(false);
		mntmAdvanced.setEnabled(GlobalConfig.getProperty("devfeatures").equals("yes"));
		MenuItem mntmDevices = new MenuItem(menu, SWT.CASCADE);
		mntmDevices.setText("端末");
		
		Menu menu_6 = new Menu(mntmDevices);
		mntmDevices.setMenu(menu_6);
		
		MenuItem mntmUpdates = new MenuItem(menu_6, SWT.CASCADE);
		mntmUpdates.setText("Updates");
		
		Menu menu_11 = new Menu(mntmUpdates);
		mntmUpdates.setMenu(menu_11);
		
		MenuItem mntmCheck = new MenuItem(menu_11, SWT.NONE);
		mntmCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Properties p = new Properties();
				Enumeration<Object> list = Devices.listDevices(false);
				while (list.hasMoreElements()) {
					DeviceEntry entry = Devices.getDevice((String)list.nextElement());
					if (entry.canShowUpdates())
						p.setProperty(entry.getId(), entry.getName());
				}
				String result = WidgetTask.openDeviceSelector(shlSonyericsson, p);
				if (result.length()>0) {
					DeviceEntry entry = new DeviceEntry(result);
					DeviceUpdates upd = new DeviceUpdates(shlSonyericsson,SWT.PRIMARY_MODAL | SWT.SHEET);
					upd.open(entry);
				}
			}
		});
		mntmCheck.setText("確認");
		
		MenuItem mntmNewItem_2 = new MenuItem(menu_11, SWT.NONE);
		mntmNewItem_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String url = WidgetTask.openUpdateURLFeeder(shlSonyericsson);
				if (url.length()>0) {
					UpdateURL u = new UpdateURL(url);
					String devId = u.getDeviceID();
					if (devId.length()>0) {
						DeviceEntry ent = Devices.getDevice(devId);
						String path = ent.getDeviceDir()+File.separator+"updates"+File.separator+u.getVariant();
						try {
							new File(path).mkdirs();
							u.dumpTo(path);
						} catch (Exception ex) {
							MyLogger.getLogger().error("Failed to write updateurl");
							ex.printStackTrace();
						}
					}
					else {
						MyLogger.getLogger().error("Model from updateurl not found in FT device database");
					}
				}
			}
		});
		mntmNewItem_2.setText("Add Update URL");
		
		MenuItem mntmCheckDrivers = new MenuItem(menu_6, SWT.NONE);
		mntmCheckDrivers.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Device.CheckAdbDrivers();
			}
		});
		mntmCheckDrivers.setText("ドライバの確認");
		
		MenuItem mntmEditor = new MenuItem(menu_6, SWT.CASCADE);
		mntmEditor.setText("管理");
		
		Menu menu_7 = new Menu(mntmEditor);
		mntmEditor.setMenu(menu_7);
		
		//MenuItem mntmEdit = new MenuItem(menu_7, SWT.NONE);
		//mntmEdit.setText("Edit");
		
		//MenuItem mntmAdd = new MenuItem(menu_7, SWT.NONE);
		//mntmAdd.setText("Add");
		
		//MenuItem mntmRemove = new MenuItem(menu_7, SWT.NONE);
		//mntmRemove.setText("Remove");
		
		MenuItem mntmExport = new MenuItem(menu_7, SWT.NONE);
		mntmExport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Devices.listDevices(true);
				String devid = WidgetTask.openDeviceSelector(shlSonyericsson);
				DeviceEntry ent = Devices.getDevice(devid);
        		if (devid.length()>0) {
        			try {
        				MyLogger.getLogger().info("Beginning export of "+ent.getName());
        				doExportDevice(devid);
        				MyLogger.getLogger().info(ent.getName()+" exported successfully");
        			}
        			catch (Exception ex) {
        				MyLogger.getLogger().error(ex.getMessage());
        			}
        		}
			}
		});
		mntmExport.setText("エクスポート");
		
		MenuItem mntmImport = new MenuItem(menu_7, SWT.NONE);
		mntmImport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Devices.listDevices(true);
        		Properties list = new Properties();
        		File[] lfiles = new File(OS.getWorkDir()+File.separator+"devices").listFiles();
        		for (int i=0;i<lfiles.length;i++) {
        			if (lfiles[i].getName().endsWith(".ftd")) {
        				String name = lfiles[i].getName();
        				name = name.substring(0,name.length()-4);        				
        				try {
        					FTDEntry entry = new FTDEntry(name);
        					list.setProperty(entry.getId(), entry.getName());
        				} catch (Exception ex) {ex.printStackTrace();}
        			}
        		}
        		if (list.size()>0) {
        			String devid = WidgetTask.openDeviceSelector(shlSonyericsson,list);
	        		if (devid.length()>0) {
						try {
							FTDEntry entry = new FTDEntry(devid);
							MsgBox.setCurrentShell(shlSonyericsson);
							FTDExplodeJob j = new FTDExplodeJob("FTD Explode job");
							j.setFTD(entry);
							j.schedule();
						}
						catch (Exception ex) {
							MyLogger.getLogger().error(ex.getMessage());
						}
	        		}
	        		else {
	        			MyLogger.getLogger().info("Import canceled");
	        		}
        		}
        		else {
        			MsgBox.error("No device to import");
        		}
			}
		});
		mntmImport.setText("インポート");
		
		MenuItem mntmHelp = new MenuItem(menu, SWT.CASCADE);
		mntmHelp.setText("ヘルプ");
		
		Menu menu_2 = new Menu(mntmHelp);
		mntmHelp.setMenu(menu_2);
		
		MenuItem mntmLogLevel = new MenuItem(menu_2, SWT.CASCADE);
		mntmLogLevel.setText("ログのレベル");
		
		Menu menu_3 = new Menu(mntmLogLevel);
		mntmLogLevel.setMenu(menu_3);
		
		MenuItem mntmError = new MenuItem(menu_3, SWT.RADIO);
		mntmError.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MyLogger.setLevel("ERROR");
				GlobalConfig.setProperty("loglevel", "error");
			}
		});
		mntmError.setText("エラー");
		
		MenuItem mntmWarning = new MenuItem(menu_3, SWT.RADIO);
		mntmWarning.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MyLogger.setLevel("WARN");
				GlobalConfig.setProperty("loglevel", "warn");
			}
		});
		mntmWarning.setText("警告");
		
		MenuItem mntmInfo = new MenuItem(menu_3, SWT.RADIO);
		mntmInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MyLogger.setLevel("INFO");
				GlobalConfig.setProperty("loglevel", "info");
			}
		});
		mntmInfo.setText("情報");
		
		MenuItem mntmDebug = new MenuItem(menu_3, SWT.RADIO);
		mntmDebug.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MyLogger.setLevel("DEBUG");
				GlobalConfig.setProperty("loglevel", "debug");
			}
		});
		mntmDebug.setText("デバッグ");
		
		MenuItem mntmAbout = new MenuItem(menu_2, SWT.NONE);
		mntmAbout.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				About about = new About(shlSonyericsson,SWT.PRIMARY_MODAL | SWT.SHEET);
				about.open();
			}
		});
		mntmAbout.setText("このアプリについて");

		if (GlobalConfig.getProperty("loglevel").equals("debug"))
			mntmDebug.setSelection(true);
		if (GlobalConfig.getProperty("loglevel").equals("warn"))
			mntmWarning.setSelection(true);
		if (GlobalConfig.getProperty("loglevel").equals("info"))
			mntmInfo.setSelection(true);
		if (GlobalConfig.getProperty("loglevel").equals("error"))
			mntmError.setSelection(true);

		ToolBar toolBar = new ToolBar(shlSonyericsson, SWT.FLAT | SWT.RIGHT);
		FormData fd_toolBar = new FormData();
		fd_toolBar.right = new FormAttachment(0, 316);
		fd_toolBar.top = new FormAttachment(0, 10);
		fd_toolBar.left = new FormAttachment(0, 10);
		toolBar.setLayoutData(fd_toolBar);
		
		tltmFlash = new ToolItem(toolBar, SWT.NONE);
		tltmFlash.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					doFlash();
				} catch (Exception ex) {}
			}
		});
		tltmFlash.setImage(SWTResourceManager.getImage(MainSWT.class, "/gui/ressources/icons/flash_32.png"));
		tltmFlash.setToolTipText("端末に書き込みます");
		
		tltmBLU = new ToolItem(toolBar, SWT.NONE);
		tltmBLU.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doBLUnlock();
			}
		});
		tltmBLU.setToolTipText("Bootloaderのロックを解除します");
		tltmBLU.setImage(SWTResourceManager.getImage(MainSWT.class, "/gui/ressources/icons/blu_32.png"));
		
		tltmRoot = new ToolItem(toolBar, SWT.NONE);
		tltmRoot.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doRoot();
			}
		});
		tltmRoot.setImage(SWTResourceManager.getImage(MainSWT.class, "/gui/ressources/icons/root_32.png"));
		tltmRoot.setEnabled(false);
		tltmRoot.setToolTipText("端末をroot化します");
		
		Button btnSaveLog = new Button(shlSonyericsson, SWT.NONE);
		btnSaveLog.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MyLogger.writeFile();
			}
		});
		FormData fd_btnSaveLog = new FormData();
		fd_btnSaveLog.right = new FormAttachment(100, -10);
		fd_btnSaveLog.left = new FormAttachment(100, -95);
		btnSaveLog.setLayoutData(fd_btnSaveLog);
		btnSaveLog.setText("ログを保存");
		
		tltmAskRoot = new ToolItem(toolBar, SWT.NONE);
		tltmAskRoot.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doAskRoot();
			}
		});
		tltmAskRoot.setImage(SWTResourceManager.getImage(MainSWT.class, "/gui/ressources/icons/askroot_32.png"));
		tltmAskRoot.setEnabled(false);
		tltmAskRoot.setToolTipText("root権限を確認します");
		
		tltmApkInstall = new ToolItem(toolBar, SWT.NONE);
		tltmApkInstall.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ApkInstaller inst = new ApkInstaller(shlSonyericsson,SWT.PRIMARY_MODAL | SWT.SHEET);
				String folder = inst.open();
				if (folder.length()>0) {
					APKInstallJob aij = new APKInstallJob("APK Install");
					aij.setFolder(folder);
					aij.schedule();
				}
				else {
					MyLogger.getLogger().info("Install APK canceled");
				}
			}
		});
		tltmApkInstall.setEnabled(false);
		tltmApkInstall.setImage(SWTResourceManager.getImage(MainSWT.class, "/gui/ressources/icons/customize_32.png"));
		
		tltmClean = new ToolItem(toolBar, SWT.NONE);
		tltmClean.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Cleaner clean = new Cleaner(shlSonyericsson,SWT.PRIMARY_MODAL | SWT.SHEET);
				DeviceApps result = clean.open();
				if (result != null) {
					CleanJob cj = new CleanJob("Clean Job");
					cj.setDeviceApps(result);
					cj.schedule();
				}
				else
					MyLogger.getLogger().info("Cleaning canceled");
				//WidgetTask.openOKBox(shlSonyericsson, "To be implemented");
			}
		});
		tltmClean.setToolTipText("ROMを掃除します");
		tltmClean.setImage(SWTResourceManager.getImage(MainSWT.class, "/gui/ressources/icons/clean_32.png"));
		tltmClean.setEnabled(false);
		
		tltmRecovery = new ToolItem(toolBar, SWT.NONE);
		tltmRecovery.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WidgetTask.openOKBox(shlSonyericsson, "To be implemented");
			}
		});
		tltmRecovery.setToolTipText("リカバリをインストールします");
		tltmRecovery.setImage(SWTResourceManager.getImage(MainSWT.class, "/gui/ressources/icons/recovery_32.png"));
		tltmRecovery.setEnabled(false);
		
		ProgressBar progressBar = new ProgressBar(shlSonyericsson, SWT.NONE);
		fd_btnSaveLog.bottom = new FormAttachment(100, -43);
		progressBar.setState(SWT.NORMAL);
		MyLogger.registerProgressBar(progressBar);
		FormData fd_progressBar = new FormData();
		fd_progressBar.left = new FormAttachment(0, 10);
		fd_progressBar.right = new FormAttachment(100, -10);
		fd_progressBar.top = new FormAttachment(btnSaveLog, 6);
		progressBar.setLayoutData(fd_progressBar);
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(shlSonyericsson, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		FormData fd_scrolledComposite = new FormData();
		fd_scrolledComposite.bottom = new FormAttachment(btnSaveLog, -6);
		fd_scrolledComposite.left = new FormAttachment(0, 10);
		fd_scrolledComposite.right = new FormAttachment(100, -10);
		scrolledComposite.setLayoutData(fd_scrolledComposite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		
		StyledText logWindow = new StyledText(scrolledComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		logWindow.setEditable(false);
		MyLogger.appendTextArea(logWindow);
		scrolledComposite.setContent(logWindow);
		scrolledComposite.setMinSize(logWindow.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		ToolBar toolBar_1 = new ToolBar(shlSonyericsson, SWT.FLAT | SWT.RIGHT);
		fd_scrolledComposite.top = new FormAttachment(toolBar_1, 2);
		FormData fd_toolBar_1 = new FormData();
		fd_toolBar_1.top = new FormAttachment(0, 10);
		fd_toolBar_1.right = new FormAttachment(btnSaveLog, 0, SWT.RIGHT);
		toolBar_1.setLayoutData(fd_toolBar_1);
		
		ToolItem tltmNewItem = new ToolItem(toolBar_1, SWT.NONE);
		tltmNewItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=PPWH7M9MNCEPA");
			}
		});
		tltmNewItem.setImage(SWTResourceManager.getImage(MainSWT.class, "/gui/ressources/icons/paypal.png"));
		MyLogger.setLevel(GlobalConfig.getProperty("loglevel").toUpperCase());
/*		try {
		Language.Init(GlobalConfig.getProperty("language").toLowerCase());
		} catch (Exception e) {
			MyLogger.getLogger().info("Language files not installed");
		}*/
		MyLogger.getLogger().info("Flashtool "+About.getVersion());
		if (JUsb.getVersion().length()>0)
			MyLogger.getLogger().info(JUsb.getVersion());
		/*vcheck = new VersionCheckerJob("Version Checker Job");
		vcheck.setMessageFrame(shlSonyericsson);
		vcheck.schedule();*/
	}

	public static void stopPhoneWatchdog() {
		DeviceChangedListener.stop();
		if (phoneWatchdog!=null) {
			phoneWatchdog.done();
			try {
				phoneWatchdog.join();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void killAdbandFastboot() {
		stopPhoneWatchdog();
	}

	public void exitProgram() {
		try {
			MyLogger.disableTextArea();
			MyLogger.setLevel(MyLogger.curlevel);
			MyLogger.getLogger().info("Stopping watchdogs and exiting ...");
			if (GlobalConfig.getProperty("killadbonexit").equals("yes")) {
				killAdbandFastboot();
			}
			vcheck.done();
		}
		catch (Exception e) {}		
	}

	public void doIdent() {
    	if (guimode) {
   		String devid = Devices.identFromRecognition();
    		if (devid.length()==0) {
    			MyLogger.getLogger().error("識別できません");
        		MyLogger.getLogger().info("選択してください");
        		devid=(String)WidgetTask.openDeviceSelector(shlSonyericsson);
    		if (devid.length()==0) {
    			MyLogger.getLogger().error("Cannot identify your device.");
        		MyLogger.getLogger().info("Selecting from user input");
        		devid=(String)WidgetTask.openDeviceSelector(shlSonyericsson);
    			if (devid.length()>0) {
        			Devices.setCurrent(devid);
        			String prop = DeviceProperties.getProperty(Devices.getCurrent().getBuildProp());
        			if (!Devices.getCurrent().getRecognition().contains(prop)) {
        			    int response = Integer.parseInt(WidgetTask.openYESNOBox(shlSonyericsson, "この端末を \n"+Devices.getCurrent().getName()+"として認識させますか?"));
        				if (response == SWT.YES)
        					Devices.getCurrent().addRecognitionToList(prop);
        			}
            		if (!Devices.isWaitingForReboot())
            			MyLogger.getLogger().info("接続した端末: " + Devices.getCurrent().getId());
        		}
        		else {
        			MyLogger.getLogger().error("You can only flash devices.");
        		}
    		}
    		else {
	    		Devices.setCurrent(devid);
				if (!Devices.isWaitingForReboot())
					MyLogger.getLogger().info("接続した端末: " + Devices.getCurrent().getName());
    		}
    		if (devid.length()>0) {
    			WidgetTask.setEnabled(mntmNoDevice, true);
    			WidgetTask.setMenuName(mntmNoDevice, " "+Devices.getCurrent().getId());
    			WidgetTask.setEnabled(mntmInstallBusybox,false);
    			if (!Devices.isWaitingForReboot()) {
    				MyLogger.getLogger().info("インストール済みbusyboxのバージョン: " + Devices.getCurrent().getInstalledBusyboxVersion(false));
    				MyLogger.getLogger().info("Androidバージョン: "+Devices.getCurrent().getVersion()+" / カーネルバージョン: "+Devices.getCurrent().getKernelVersion()+" / ビルド番号: "+Devices.getCurrent().getBuildId());
    			}
    			if (Devices.getCurrent().isRecovery()) {
    				MyLogger.getLogger().info("リカバリモードです");
    				WidgetTask.setEnabled(tltmRoot,false);
    				WidgetTask.setEnabled(tltmAskRoot,false);
    				WidgetTask.setEnabled(tltmApkInstall,false);
    				doGiveRoot(true);
    			}
    			else {
    				boolean hasSU = Devices.getCurrent().hasSU();
    				WidgetTask.setEnabled(tltmRoot, !hasSU);
    				WidgetTask.setEnabled(tltmApkInstall, true);
    				if (hasSU) {
        				MyLogger.getLogger().info("root権限の確認中");
    					boolean hasRoot = Devices.getCurrent().hasRoot();
						doGiveRoot(hasRoot);
    					if (hasRoot) {
    						doInstFlashtool();
    					}	
    				}
    			}
    			MyLogger.getLogger().debug("Now setting buttons availability - btnRoot");
    			MyLogger.getLogger().debug("mtmRootzergRush menu");
    			/*mntmRootzergRush.setEnabled(true);
    			MyLogger.getLogger().debug("mtmRootPsneuter menu");
    			mntmRootPsneuter.setEnabled(true);
    			MyLogger.getLogger().debug("mtmRootEmulator menu");
    			mntmRootEmulator.setEnabled(true);
    			MyLogger.getLogger().debug("mtmRootAdbRestore menu");
    			mntmRootAdbRestore.setEnabled(true);
    			MyLogger.getLogger().debug("mtmUnRoot menu");
    			mntmUnRoot.setEnabled(true);*/

    			boolean flash = Devices.getCurrent().canFlash();
    			MyLogger.getLogger().debug("flashBtn button "+flash);
    			WidgetTask.setEnabled(tltmFlash,flash);
    			//MyLogger.getLogger().debug("custBtn button");
    			//custBtn.setEnabled(true);
    			MyLogger.getLogger().debug("Now adding plugins");
    			//mnPlugins.removeAll();
    			//addDevicesPlugins();
    			//addGenericPlugins();
    			MyLogger.getLogger().debug("Stop waiting for device");
    			if (Devices.isWaitingForReboot())
    				Devices.stopWaitForReboot();
    			MyLogger.getLogger().debug("End of identification");
    		}
    	}
	}

	public void doGiveRoot(boolean hasRoot) {
		/*btnCleanroot.setEnabled(true);
		mntmInstallBusybox.setEnabled(true);
		mntmClearCache.setEnabled(true);
		mntmBuildpropEditor.setEnabled(true);
		if (new File(OS.getWorkDir()+fsep+"devices"+fsep+Devices.getCurrent().getId()+fsep+"rebrand").isDirectory())
			mntmBuildpropRebrand.setEnabled(true);
		mntmRebootIntoRecoveryT.setEnabled(Devices.getCurrent().canRecovery());
		mntmRebootDefaultRecovery.setEnabled(true);
		mntmSetDefaultRecovery.setEnabled(Devices.getCurrent().canRecovery());
		mntmSetDefaultKernel.setEnabled(Devices.getCurrent().canKernel());
		mntmRebootCustomKernel.setEnabled(Devices.getCurrent().canKernel());
		mntmRebootDefaultKernel.setEnabled(true);
		//mntmInstallBootkit.setEnabled(true);
		//mntmRecoveryControler.setEnabled(true);
		mntmBackupSystemApps.setEnabled(true);
		btnXrecovery.setEnabled(Devices.getCurrent().canRecovery());
		btnKernel.setEnabled(Devices.getCurrent().canKernel());*/
		WidgetTask.setEnabled(tltmAskRoot,!hasRoot);
		WidgetTask.setEnabled(mntmInstallBusybox,hasRoot);
		WidgetTask.setEnabled(tltmClean,hasRoot);
		WidgetTask.setEnabled(tltmRecovery,hasRoot&&Devices.getCurrent().canRecovery());
		WidgetTask.setEnabled(mntmRawRestore,hasRoot);
		WidgetTask.setEnabled(mntmRawBackup,hasRoot);
		WidgetTask.setEnabled(tltmAskRoot,!hasRoot);
		if (!Devices.isWaitingForReboot())
			if (hasRoot) {
				MyLogger.getLogger().info("rootアクセスが許可されました");
				AdbUtility.antiRic();
			}
			else
				MyLogger.getLogger().info("rootアクセスが拒否されました");
    }

	public void doAskRoot() {
		Job job = new Job("Give Root") {
			protected IStatus run(IProgressMonitor monitor) {
				MyLogger.getLogger().warn("端末のプロンプトで許可してください!");
        		boolean hasRoot = Devices.getCurrent().hasRoot();
        		doGiveRoot(hasRoot);
        		return Status.OK_STATUS;				
			}
		};
		job.schedule();
	}

	public void doInstFlashtool() {
		try {
			if (!AdbUtility.exists("/system/flashtool")) {
				Devices.getCurrent().doBusyboxHelper();
				MyLogger.getLogger().info("端末にtoolboxをインストール中...");
				AdbUtility.push(OS.getWorkDir()+File.separator+"custom"+File.separator+"root"+File.separator+"ftkit.tar",GlobalConfig.getProperty("deviceworkdir"));
				FTShell ftshell = new FTShell("installftkit");
				ftshell.runRoot();
			}
		}
		catch (Exception e) {
			MyLogger.getLogger().error(e.getMessage());
		}
    }

	public void doFlash() throws Exception {
		String select = WidgetTask.openBootModeSelector(shlSonyericsson);
		if (select.equals("flashmode")) {
			doFlashmode("","");
		}
		else if (select.equals("fastboot"))
			doFastBoot();
		else
			MyLogger.getLogger().info("中止しました");
	}
	
	public void doFastBoot() throws Exception {
		FastbootToolbox fbbox = new FastbootToolbox(shlSonyericsson,SWT.PRIMARY_MODAL | SWT.SHEET);
		fbbox.open();
	}
	
	public void doFlashmode(final String pftfpath, final String pftfname) throws Exception {
		try {
			FTFSelector ftfsel = new FTFSelector(shlSonyericsson,SWT.PRIMARY_MODAL | SWT.SHEET);
			final Bundle bundle = (Bundle)ftfsel.open(pftfpath, pftfname);
			MyLogger.getLogger().info("選択中: "+bundle);
			if (bundle !=null) {
				bundle.setSimulate(GlobalConfig.getProperty("simulate").toLowerCase().equals("yes"));
				final X10flash flash = new X10flash(bundle,shlSonyericsson);
				try {
						FlashJob fjob = new FlashJob("Flash");
						fjob.setFlash(flash);
						fjob.setShell(shlSonyericsson);
						fjob.schedule();
				}
				catch (Exception e){
					MyLogger.getLogger().error(e.getMessage());
					MyLogger.getLogger().info("中止しました");
					if (flash.getBundle()!=null)
						flash.getBundle().close();
				}
			}
			else
				MyLogger.getLogger().info("中止しました");

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		

		
		/*Worker.post(new Job() {
			public Object run() {
				System.out.println("flashmode");
				if (bundle!=null) {
					X10flash flash=null;
					try {
			    		MyLogger.getLogger().info("Preparing files for flashing");
			    		bundle.open();
				    	bundle.setSimulate(GlobalConfig.getProperty("simulate").toLowerCase().equals("yes"));
						flash = new X10flash(bundle);
						
						/*if ((new WaitDeviceFlashmodeGUI(flash)).deviceFound(_root)) {
				    		try {
								flash.openDevice();
								flash.flashDevice();
				    		}
				    		catch (Exception e) {
				    			e.printStackTrace();
				    		}
						}
					}
					catch (BundleException ioe) {
						MyLogger.getLogger().error("Error preparing files");
					}
					catch (Exception e) {
						MyLogger.getLogger().error(e.getMessage());
					}
					bundle.close();
				}
				else MyLogger.getLogger().info("Flash canceled");
				return null;
			}
		});*/
	}

	public void doBLUnlock() {
		try {
			final X10flash flash = new X10flash(new Bundle(),shlSonyericsson);
			MyLogger.getLogger().info("端末をflashmodeで接続してください");
			String result = (String)WidgetTask.openWaitDeviceForFlashmode(shlSonyericsson,flash);
			if (result.equals("OK")) {
				try {
					GetULCodeJob ulj = new GetULCodeJob("Unlock code");
					ulj.setFlash(flash);
					ulj.addJobChangeListener(new IJobChangeListener() {
						public void aboutToRun(IJobChangeEvent event) {}
						public void awake(IJobChangeEvent event) {}
						public void running(IJobChangeEvent event) {}
						public void scheduled(IJobChangeEvent event) {}
						public void sleeping(IJobChangeEvent event) {}
					
						public void done(IJobChangeEvent event) {
							GetULCodeJob j = (GetULCodeJob)event.getJob();
							if (j.getPhoneCert().length()>0) {
								OldUnlockJob uj = new OldUnlockJob("Unlock 2010");
								uj.setPhoneCert(j.getPhoneCert());
								uj.setPlatform(j.getPlatform());
								uj.setStatus(j.getBLStatus());
								uj.schedule();
							}
							else {
								String ulcode=j.getULCode();
								String imei = j.getIMEI();
								String blstatus = j.getBLStatus();
								String serial = j.getSerial();
								if (!j.alreadyUnlocked()) {
									if (!blstatus.equals("ROOTABLE")) {
										MyLogger.getLogger().info("Your phone bootloader cannot be officially unlocked");
										MyLogger.getLogger().info("You can now unplug and restart your phone");
									}
									else {
										MyLogger.getLogger().info("Now unplug your device and restart it into fastbootmode");
										String result = (String)WidgetTask.openWaitDeviceForFastboot(shlSonyericsson);
										if (result.equals("OK")) {
											WidgetTask.openBLWizard(shlSonyericsson, serial, imei, ulcode, null, "U");
										}
										else {
											MyLogger.getLogger().info("Bootloader unlock canceled");
										}
									}
								}
								else {
									WidgetTask.openBLWizard(shlSonyericsson, serial, imei, ulcode, flash, j.isRelocked()?"U":"R");
									flash.closeDevice();
									MyLogger.initProgress(0);
									MyLogger.getLogger().info("You can now unplug and restart your device");
									DeviceChangedListener.pause(false);								
								}
							}
						}
				});
				ulj.schedule();
			}
			catch (Exception e) {
				flash.closeDevice();
				DeviceChangedListener.pause(false);
				MyLogger.getLogger().info("Bootloader unlock canceled");
				MyLogger.initProgress(0);
			}
		}
		else {
			MyLogger.getLogger().info("Bootloader unlock canceled");
		}
		}
		catch (Exception e) {
			MyLogger.getLogger().error(e.getMessage());
			e.printStackTrace();
		}
	}

	public void doExportDevice(String device) throws Exception {
		File ftd = new File(OS.getWorkDir()+OS.getFileSeparator()+"devices"+OS.getFileSeparator()+device+".ftd");
		byte buffer[] = new byte[10240];
	    FileOutputStream stream = new FileOutputStream(ftd);
	    JarOutputStream out = new JarOutputStream(stream);
	    out.setLevel(Deflater.BEST_SPEED);
	    File root = new File(OS.getWorkDir()+OS.getFileSeparator()+"devices"+OS.getFileSeparator()+device);
	    int rootindex = root.getAbsolutePath().length();
		Collection<File> c = OS.listFileTree(root);
		Iterator<File> i = c.iterator();
		while (i.hasNext()) {
			File entry = i.next();
			String name = entry.getAbsolutePath().substring(rootindex-device.length());
			if (entry.isDirectory()) name = name+"/";
		    JarEntry jarAdd = new JarEntry(name);
	        out.putNextEntry(jarAdd);
	        if (!entry.isDirectory()) {
	        InputStream in = new FileInputStream(entry);
	        while (true) {
	          int nRead = in.read(buffer, 0, buffer.length);
	          if (nRead <= 0)
	            break;
	          out.write(buffer, 0, nRead);
	        }
	        in.close();
	        }
		}
		out.close();
	    stream.close();
	}

	public void doBackupTA() {
		WidgetTask.openOKBox(shlSonyericsson, "WARNING : This action will not create a backup of your TA.");
		Bundle bundle = new Bundle();
		bundle.setSimulate(GlobalConfig.getProperty("simulate").toLowerCase().equals("yes"));
		final X10flash flash = new X10flash(bundle,shlSonyericsson);
		try {
			MyLogger.getLogger().info("端末をflashmodeで接続してください。");
			String result = (String)WidgetTask.openWaitDeviceForFlashmode(shlSonyericsson,flash);
			if (result.equals("OK")) {
				BackupTAJob fjob = new BackupTAJob("Flash");
				fjob.setFlash(flash);
				fjob.schedule();
			}
			else
				MyLogger.getLogger().info("Flash canceled");
		}
		catch (Exception e){
			MyLogger.getLogger().error(e.getMessage());
			MyLogger.getLogger().info("Flash canceled");
			if (flash.getBundle()!=null)
				flash.getBundle().close();
		}
	}

	public void doRoot() {
		String pck = WidgetTask.openRootPackageSelector(shlSonyericsson);
		RootJob rj = new RootJob("Root device");
		rj.setRootPackage(pck);
		rj.setParentShell(shlSonyericsson);
		if (Devices.getCurrent().getVersion().contains("2.3")) {
			rj.setAction("doRootzergRush");
		}
		else
			if (!Devices.getCurrent().getVersion().contains("4.0") && !Devices.getCurrent().getVersion().contains("4.1"))
				rj.setAction("doRootpsneuter");
			else {
				if (Devices.getCurrent().getVersion().contains("4.0.3"))
					rj.setAction("doRootEmulator");
				else
					if (Devices.getCurrent().getVersion().contains("4.0"))
						rj.setAction("doRootAdbRestore");
					else {
						if (Devices.getCurrent().getVersion().contains("4.1")) {
							rj.setAction("doRootServiceMenu");							
						/*}
						else
							if (Devices.getCurrent().getVersion().contains("4.2")) {
								rj.setAction("doRootPerfEvent");*/			
							}
							else {
								MessageBox mb = new MessageBox(shlSonyericsson,SWT.ICON_ERROR|SWT.OK);
								mb.setText("Errorr");
								mb.setMessage("No root exploit for your device");
								int result = mb.open();
							}
					}
			}
		rj.schedule();
	}
	
	public void doRoot(String rootmethod) {
		String pck = WidgetTask.openRootPackageSelector(shlSonyericsson);
		RootJob rj = new RootJob("Root device");
		rj.setRootPackage(pck);
		rj.setParentShell(shlSonyericsson);
		rj.setAction(rootmethod);							
		rj.schedule();		
	}

	public void doYaffs2Unpack() {
		FileDialog dlg = new FileDialog(shlSonyericsson);
        dlg.setFilterExtensions(new String[]{"*.yaffs2"});
        dlg.setText("YAFFS2 File Chooser");
        String dir = dlg.open();
        if (dir != null) {
        	Yaffs2Job yj = new Yaffs2Job("YAFFS2 Extractor");
        	yj.setFilename(dir);
        	yj.schedule();
        }
        else
        	MyLogger.getLogger().info("Canceled");
	}
}