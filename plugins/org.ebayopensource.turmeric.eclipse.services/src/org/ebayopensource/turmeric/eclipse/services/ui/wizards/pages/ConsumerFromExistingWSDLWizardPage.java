/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
/**
 * 
 */
package org.ebayopensource.turmeric.eclipse.services.ui.wizards.pages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Service;

import org.apache.commons.lang.StringUtils;
import org.ebayopensource.turmeric.eclipse.exception.validation.ValidationInterruptedException;
import org.ebayopensource.turmeric.eclipse.logging.SOALogger;
import org.ebayopensource.turmeric.eclipse.registry.ExtensionPointFactory;
import org.ebayopensource.turmeric.eclipse.registry.intf.IClientRegistryProvider;
import org.ebayopensource.turmeric.eclipse.registry.models.ClientAssetModel;
import org.ebayopensource.turmeric.eclipse.repositorysystem.core.GlobalRepositorySystem;
import org.ebayopensource.turmeric.eclipse.repositorysystem.core.ISOAHelpProvider;
import org.ebayopensource.turmeric.eclipse.resources.constants.SOAProjectConstants;
import org.ebayopensource.turmeric.eclipse.resources.util.SOAIntfUtil;
import org.ebayopensource.turmeric.eclipse.resources.util.SOAServiceUtil;
import org.ebayopensource.turmeric.eclipse.services.resources.SOAMessages;
import org.ebayopensource.turmeric.eclipse.ui.AbstractSOADomainWizard;
import org.ebayopensource.turmeric.eclipse.utils.lang.StringUtil;
import org.ebayopensource.turmeric.eclipse.utils.plugin.EclipseMessageUtils;
import org.ebayopensource.turmeric.eclipse.utils.ui.UIUtil;
import org.ebayopensource.turmeric.eclipse.validator.core.ErrorMessage;
import org.ebayopensource.turmeric.eclipse.validator.core.InputObject;
import org.ebayopensource.turmeric.eclipse.validator.utils.ValidateUtil;
import org.ebayopensource.turmeric.eclipse.validator.utils.common.NameValidator;
import org.ebayopensource.turmeric.eclipse.validator.utils.common.RegExConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


/**
 * @author yayu
 *
 */
public class ConsumerFromExistingWSDLWizardPage extends AbstractNewServiceFromWSDLWizardPage{
	private Text serviceClientText;
	private Text consumerID;
	private Text adminText;
	private Button retrieveConsumerIDBtn;
	private ListViewer envrionmentList;
	private List<String> environments = new ArrayList<String>();
	private String versionFromWSDL = null;

	private static final SOALogger logger = SOALogger.getLogger();

	public ConsumerFromExistingWSDLWizardPage(
			final IStructuredSelection selection) {
		super(
				"newConsumerFromWSDLWizardPage",
				"New Consumer From Existing WSDL Wizard",
				"This wizard creates a new SOA Service Consumer from a pre-existing WSDL document.");
	}

	public ConsumerFromExistingWSDLWizardPage() {
		this(null);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible == true) {
			dialogChanged(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createControl(Composite parent) {
		try {
			final Composite container = super.createParentControl(parent, 4);

			Text wsdlText = addWSDL(container);
			wsdlText.setFocus();
			addWorkspaceRootChooser(container);
			addServiceDomainList(container, true);
			if (serviceDomainList != null && domainClassifierList != null) {
				this.serviceDomainList.select(-1);
				this.serviceDomainList.clearSelection();
				this.domainClassifierList.select(-1);
				this.domainClassifierList.clearSelection();
			}
			addServiceVersion(container);
			addServiceName(container, false);
			this.adminText = addAdminName(container, false);
			{
				if (this.resourceNameControlDecoration != null) {
					// we do not want to show both WARNING and INFORMATION icons
					resourceNameControlDecoration.hide();
				}
				ControlDecoration controlDecoration = new ControlDecoration(
						adminText, SWT.LEFT | SWT.TOP);
				controlDecoration.setShowOnlyOnFocus(false);
				controlDecoration
						.setDescriptionText(SOAMessages.WARNING_ADMIN_NAME_MANUAL_OVERRIDE);
				FieldDecoration fieldDecoration = FieldDecorationRegistry
						.getDefault().getFieldDecoration(
								FieldDecorationRegistry.DEC_WARNING);
				controlDecoration.setImage(fieldDecoration.getImage());
			}
			addTargetNamespace(container, null, false);
			createServiceClient(container);
			createConsumerIDText(container);
			createBaseConsumerSource(container);
			addServicePackage(container);
			addServiceLayer(container);
			createEnvironmentList(container);
			addWSDLPackageToNamespace(container);
			addTypeFolding(container);
			super.setTypeFolding(false);
		} catch (Exception e) {
			logger.error(e);
			UIUtil.showErrorDialog(e);
		}
	}

	protected Text createConsumerIDText(Composite parent) throws CoreException {
		this.consumerID = super.createLabelTextField(parent, "Consumer &ID:",
				"", modifyListener, false, true,
				"the consumer ID of the new service consumer");
		final IClientRegistryProvider clientRegProvider = ExtensionPointFactory
				.getSOAClientRegistryProvider();
		if (clientRegProvider != null) {
			// The retrieve button should only be created if AR plugin is
			// available
			retrieveConsumerIDBtn = new Button(parent, SWT.PUSH);
			retrieveConsumerIDBtn.setText("Retrie&ve");
			retrieveConsumerIDBtn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						ClientAssetModel clientModel = clientRegProvider
								.getClientAsset(getClientName());
						if (clientModel != null) {
							String conID = StringUtils.isBlank(clientModel
									.getConsumerId()) ? "" : clientModel
									.getConsumerId();
							consumerID.setText(conID);
							/*
							 * if (StringUtils.isNotBlank(conID))
							 * retrieveConsumerIDBtn.setEnabled(false);
							 */
						}
					} catch (Exception e1) {
						SOALogger.getLogger().error(e1);
						UIUtil.showErrorDialog(e1);
					}
				}
			});

			final Text text = getResourceNameText();
			text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					retrieveConsumerIDBtn.setEnabled(StringUtils
							.isNotBlank(getResourceName()));

				}
			});
		} else {
			// AR plugin is not available
			super.createEmptyLabel(parent, 1);
		}

		// TODO get consumer ID if possible
		return this.consumerID;
	}

	protected ListViewer createEnvironmentList(Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText("Environments");
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 4;
		group.setLayoutData(data);
		group.setLayout(new GridLayout(2, false));
		envrionmentList = new ListViewer(group, SWT.SINGLE | SWT.BORDER);
		envrionmentList.getList().setLayoutData(
				new GridData(GridData.FILL_BOTH));

		envrionmentList.setContentProvider(new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {
				return environments.toArray();
			}

			public void dispose() {
				
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			
			}
			
		});
		envrionmentList.setLabelProvider(new ILabelProvider() {

			public Image getImage(Object element) {
				return null;
			}

			public String getText(Object element) {
				return String.valueOf(element);
			}

			public void addListener(ILabelProviderListener listener) {
				
			}

			public void dispose() {
				
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void removeListener(ILabelProviderListener listener) {
				
			}
			
		});
		environments.add(SOAProjectConstants.DEFAULT_CLIENT_CONFIG_ENVIRONMENT);
		envrionmentList.setInput(environments);
		
		Composite btnComposite = new Composite(group, SWT.NONE);
		btnComposite.setLayout(new GridLayout(1, true));
		btnComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL));

		final Button addBtn = new Button(btnComposite, SWT.PUSH);
		addBtn.setText("Add...");
		addBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final IInputValidator validator = new IInputValidator() {

					public String isValid(String newText) {
						if (StringUtils.isBlank(newText)) {
							return "Error: Environment name must not be empty";
						}
						if (environments.contains(newText)) {
							return "Error: Environment name already exist->"
									+ newText;
						}
						final InputObject inputObject = new InputObject(
								newText, RegExConstants.PROJECT_NAME_EXP,
								ErrorMessage.CLIENT_NAME_ERRORMSG);

						try {
							IStatus validationModel = NameValidator
									.getInstance().validate(inputObject);
							if (validationModel != null
									&& validationModel.getSeverity() == IStatus.ERROR) {
								return ValidateUtil
										.getBasicFormattedUIErrorMessage(validationModel);
							}
						} catch (ValidationInterruptedException e) {
							processException(e);
						}
						return null;
					}

				};
				InputDialog dialog = new InputDialog(UIUtil.getActiveShell(),
						"New Environment",
						"Please enter the new environment name", "", validator);
				if (dialog.open() == Window.OK) {
					environments.add(dialog.getValue());
					envrionmentList.refresh();
					dialogChanged();
				}
			}
		});

		final Button removeBtn = new Button(btnComposite, SWT.PUSH);
		removeBtn.setText("Remove");
		removeBtn.setEnabled(false);
		removeBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Object obj = ((IStructuredSelection) envrionmentList
						.getSelection()).getFirstElement();
				environments.remove(obj);
				envrionmentList.refresh();
				dialogChanged();
			}
		});
		envrionmentList.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				removeBtn.setEnabled(envrionmentList.getSelection()
						.isEmpty() == false);
			}
		}
		);
		
		UIUtil.setEqualWidthHintForButtons(addBtn, removeBtn);
		return envrionmentList;
	}

	protected Composite createServiceClient(final Composite parent) {
		return createServiceClient(parent, true);
	}

	protected Composite createServiceClient(final Composite parent,
			final boolean editable) {
		new Label(parent, SWT.LEFT).setText("&Client Name:");
		serviceClientText = new Text(parent, SWT.BORDER);
		serviceClientText.setEditable(editable);
		serviceClientText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 2, 1));
		serviceClientText.addModifyListener(modifyListener);
		createEmptyLabel(parent, 1);
		UIUtil.decorateControl(this, serviceClientText,
				"the client name of the consumer");
		return parent;
	}

	@Override
	protected boolean dialogChanged() {
		
		if (publicServiceNameText != null
				&& (validateName(
						publicServiceNameText,
						getPublicServiceName(),
						RegExConstants.SERVICE_NAME_EXP,
						"The service name ["
								+ getPublicServiceName()
								+ "] in WSDL file is not valid against the pattern \""
								+ RegExConstants.SERVICE_NAME_EXP
								+ "\". Please correct service name in WSDL and run this wizard again.") == false)) {
			return false;
		}
		
		
		if (super.dialogChanged() == false && isPageComplete() == false)
			return false;
		

		if (this.serviceClientText != null) {
			if (StringUtils.isBlank(getClientName())) {
				updateStatus(this.serviceClientText,
						"Client name must be specified");
				return false;
			}
			if (StringUtils.equals(StringUtils.capitalize(getClientName()),
					getClientName()) == false) {
				updateStatus(this.serviceClientText,
						"Client name must be capitalized.");
				return false;
			}
			if (validateName(this.serviceClientText, getClientName(),
					RegExConstants.PROJECT_NAME_EXP,
					ErrorMessage.PROJECT_NAME_ERRORMSG + " The name ["
							+ getClientName()
							+ "] is not valid against the pattern \""
							+ RegExConstants.PROJECT_NAME_EXP + "\"") == false) {
				return false;
			}
		}

		if (envrionmentList != null) {
			if (environments.isEmpty()) {
				updateStatus(envrionmentList.getControl(),
						"At least one environment must be added");
				return false;
			}
		}

		// String version = resourceVersionText.getText();
		// if (StringUtils.isNotBlank(version) &&
		// (isV2Format == true)) {
		// // version from WSDL is X.Y, only maintenance version should be
		// changed.
		// Version newVer = new Version(version);
		// Version oldVer = new Version(versionFromWSDL);
		// if ((newVer.getMajor() != oldVer.getMajor())
		// || (newVer.getMinor() != oldVer.getMinor())) {
		// updateStatus(super.resourceVersionText,
		// "Service version from WSDL is: "+versionFromWSDL +
		// ". You can not change the major and minor version. The version must start with->"
		// + versionFromWSDL);
		// return false;
		// }
		// }

		/*
		 * 1) If service version in WSDL follows V3 format, like 1.2.3, service
		 * version text will not be editable. 2) If service version in WSDL
		 * doesn�t follow V3 format, like 1.2, 1.2,3, 1, 1.a, 1.2.a, then
		 * service version text is editable. BUT even user specified a correct
		 * V3 version, there will be an error marker on the service version text
		 * says Specified service version [1.2.3] does not match service version
		 * in WSDL [1.2]. Please modify service version in source WSDL and
		 * follow format {major.minor.maintance}�. It means the WSDL file used
		 * in wizard must contain a correct V3 format service version.
		 * Otherwise, the wizard couldn�t continue.
		 */

		if ((versionFromWSDL != null)
				&& versionFromWSDL.equals(getResourceVersion()) == false) {
			String errorMsg = StringUtil.formatString(
					SOAMessages.DIFFERENT_SERVICE_VERSION_WITH_WSDL, getResourceVersion(),
					versionFromWSDL);
			updateStatus(super.resourceVersionText, errorMsg);
			return false;
		}

		if (domainClassifierList != null
				&& StringUtils.isNotBlank(getDomainClassifier())
				&& this.wsdl != null) {
			String namespacePart = getOrganizationProvider()
					.getNamespacePartFromTargetNamespace(
							this.wsdl.getTargetNamespace());
			if (StringUtils.isNotBlank(namespacePart)
					&& namespacePart.equals(getDomainClassifier()) == false) {
				// user has selected a namespace part that not match the ns-part
				// from the wsdl file
				updateStatus(super.domainClassifierList, StringUtil
						.formatString(SOAMessages.ERR_WRONG_NAMESPACEPART,
								getDomainClassifier(), this.wsdl
										.getTargetNamespace()));
				return false;
			}
		}

		if (StringUtils.isNotEmpty(getResourceName())
				&& Character.isLowerCase(getResourceName().charAt(0))) {
			updatePageStatus(getResourceNameText(), EclipseMessageUtils
					.createStatus(SOAMessages.SVCNAME_ERR, Status.WARNING));
			return true;
		}

		return true;
	}

	@Override
	public void wsdlChanged(final Definition wsdl) {
		final Collection<?> services = wsdl.getServices().values();
		if (services.size() > 0) {
			// only process the first service
			final Service service = (Service) services.toArray()[0];
			if (services.size() > 1) {
				logger
						.warning("Found multiple service, but only the first service will be processed->"
								+ service.getQName());
			}
			final String targetNs = wsdl.getTargetNamespace();
			targetNamespaceModified(targetNs);
			setTargetNamespace(targetNs);
			if (domainClassifierList == null) {
				// Non-MP
				String nsPart = StringUtils
						.capitalize(getOrganizationProvider()
								.getNamespacePartFromTargetNamespace(targetNs));
				if (StringUtils.isNotBlank(nsPart)) {
					this.adminText.setText(nsPart + getAdminName());
				} else {
					this.adminText
							.setText(getPublicServiceName()
									+ SOAProjectConstants.MAJOR_VERSION_PREFIX
									+ SOAServiceUtil
											.getServiceMajorVersion(getServiceVersion()));
				}
			}
			String version = SOAIntfUtil.getServiceVersionFromWsdl(wsdl,
					getPublicServiceName()).trim();
			resourceVersionText.setEditable(true);
			if (StringUtils.isNotBlank(version)) {
				versionFromWSDL = version;
				// has version
				int versionPart = StringUtils.countMatches(version,
						SOAProjectConstants.DELIMITER_DOT);
				// add "dot number" to version. It will be changed to X.Y.Z
				if (versionPart == 2) {
					// is new version format, set version text read-only.
					resourceVersionText.setEditable(false);
				} else {
					// is v2format
					while (versionPart < 2) {
						version += SOAProjectConstants.DELIMITER_DOT + "0";
						versionPart++;
					}
				}
				resourceVersionText.setText(version);
			} else {
				// don't have version, use default version.
				resourceVersionText
						.setText(SOAProjectConstants.DEFAULT_SERVICE_VERSION);
			}
			serviceClientText.setText(getAdminName()
					+ SOAProjectConstants.CLIENT_PROJECT_SUFFIX);
		} else {
			serviceClientText.setText(DEFAULT_TEXT_VALUE);
		}
	}

	@Override
	protected void targetNamespaceModified(String newNamespace) {
		super.targetNamespaceModified(newNamespace);
		if (this.serviceDomainList == null || this.domainClassifierList == null
				|| StringUtils.isBlank(newNamespace))
			return;

		String namespacePart = getOrganizationProvider()
				.getNamespacePartFromTargetNamespace(newNamespace);

		if (StringUtils.isNotBlank(namespacePart)) {
			String domainName = StringUtils.capitalize(namespacePart);
			Map<String, List<String>> domainList = Collections.emptyMap();
			if (getWizard() instanceof AbstractSOADomainWizard) {
				try {
					domainList = ((AbstractSOADomainWizard) getWizard())
							.getDomainList();
				} catch (Exception e) {
					logger.warning(e);
				}
			}

			for (String key : domainList.keySet()) {
				final List<String> values = domainList.get(key);
				if (values != null && values.contains(namespacePart)) {
					domainName = key;
					break;
				}
			}
			this.serviceDomainList.setText(domainName);
			this.domainClassifierList.setText(namespacePart);
		} else if (StringUtils.isBlank(getServiceDomain())
				|| StringUtils.isBlank(getDomainClassifier())) {
			// could not get the namespace-part
			this.serviceDomainList.select(-1);
			this.serviceDomainList.clearSelection();
			this.domainClassifierList.clearSelection();
		}
	}

	@Override
	public void resetServiceName() {
		super.resetServiceName();
		if (serviceClientText != null)
			serviceClientText.setText(DEFAULT_TEXT_VALUE);
	}
	
	public String getClientName() {
		return super.getTextValue(this.serviceClientText);
	}
	
	public String getConsumerId() {
		return super.getTextValue(this.consumerID);
	}
	
	@Override
	public List<ProjectNameControl> getProjectNames() {
		final List<ProjectNameControl> result = super.getProjectNames();
		if (this.serviceClientText != null) {
			result.add(new ProjectNameControl(getClientName(),
					this.serviceClientText));
		}
		return result;
	}
	
	public List<String> getEnvironments() {
		return environments;
	}

	@Override
	public String getDefaultResourceName() {
		final String defaultName = computeServiceName();
		if (StringUtils.isNotBlank(defaultName))
			return defaultName;
		else
			return "";
	}

	@Override
	public String getHelpContextID() {
		return GlobalRepositorySystem.instanceOf().getActiveRepositorySystem()
				.getHelpProvider().getHelpContextID(
						ISOAHelpProvider.PAGE_CREATE_CONSUMER_FROM_WSDL);
	}
	
	
}