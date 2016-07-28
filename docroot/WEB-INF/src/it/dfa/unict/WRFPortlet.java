package it.dfa.unict;

import it.dfa.unict.util.Constants;
import it.dfa.unict.util.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.ProcessAction;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletConfig;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

public class WRFPortlet extends MVCPortlet {

	private final Log _log = LogFactoryUtil.getLog(WRFPortlet.class);

	public static String pilotScript;

	/**
	 * Initializes portlet's configuration with pilot-script file path.
	 * 
	 * @see com.liferay.util.bridges.mvc.MVCPortlet#init()
	 */
	@Override
	public void init() throws PortletException {
		super.init();
		pilotScript = getPortletContext().getRealPath(Constants.FILE_SEPARATOR)
				+ "WEB-INF/job/" + getInitParameter("pilot-script");
	}

	/**
	 * Processes the 'multipart/form-data' form uploading file, gets the jobs
	 * label finally submits job towards a list of enabled DCI specified in the
	 * configuration.
	 * 
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 * @throws PortletException
	 */
	@ProcessAction(name = "submit")
	public void submit(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException, PortletException {

		AppInput appInput = new AppInput();
		PortletPreferences preferences = actionRequest.getPreferences();

		String JSONAppPrefs = GetterUtil.getString(preferences.getValue(
				Constants.APP_PREFERENCES, null));
		AppPreferences appPrefs = Utils.getAppPreferences(JSONAppPrefs);

		SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.TS_FORMAT);
		String timestamp = dateFormat.format(Calendar.getInstance().getTime());
		appInput.setTimestamp(timestamp);

		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest
				.getAttribute(WebKeys.THEME_DISPLAY);
		User user = themeDisplay.getUser();
		String username = user.getScreenName();
		appInput.setUsername(username);
		UploadPortletRequest uploadRequest = PortalUtil
				.getUploadPortletRequest(actionRequest);

		File uploadedFile = processInputFile(uploadRequest, username,
				timestamp, appInput);

		if (uploadedFile != null && uploadedFile.length() == 0) {
			SessionErrors.add(actionRequest, "empty-file");
		} else {

			if (uploadedFile != null)
				appInput.setInputSandbox(uploadedFile.getAbsolutePath());

			String joblabel = ParamUtil.getString(uploadRequest, "jobLabel");

			appInput.setJobLabel(joblabel);

			_log.info(appInput);

			// List<AppInfrastructureInfo> enabledInfras = Utils
			// .getEnabledInfrastructureInfo(JSONAppInfras);
			//
			// if (enabledInfras.size() > 0) {
			// InfrastructureInfo infrastructureInfo[] = Utils
			// .convertAppInfrastructureInfo(enabledInfras);
			//
			// submitJob(appPrefs, appInput, infrastructureInfo);
			//
			// PortalUtil.copyRequestParameters(actionRequest, actionResponse);
			// actionResponse.setRenderParameter("jobLabel", joblabel);
			// actionResponse
			// .setRenderParameter("jspPage", "/jsps/submit.jsp");
			// }
		}

		// Hide default Liferay success/error messages
		PortletConfig portletConfig = (PortletConfig) actionRequest
				.getAttribute(JavaConstants.JAVAX_PORTLET_CONFIG);
		LiferayPortletConfig liferayPortletConfig = (LiferayPortletConfig) portletConfig;
		SessionMessages.add(actionRequest, liferayPortletConfig.getPortletId()
				+ SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
	}

	/**
	 * Method responsible of job submission.
	 * 
	 * @param preferences
	 *            Application preferences object.
	 * @param appInput
	 *            An object consists of view input fields values and other job
	 *            input parameters.
	 * @param enabledInfrastructures
	 *            List of enabled infrastructures.
	 */
	private void submitJob(AppPreferences preferences, AppInput appInput) {

		_log.info("Submit not implemented yet!!!");

	}

	/**
	 * @param uploadRequest
	 * @param username
	 * @param timestamp
	 * @param appInput
	 * @return
	 * @throws IOException
	 */
	private File processInputFile(UploadPortletRequest uploadRequest,
			String username, String timestamp, AppInput appInput)
			throws IOException {

		File file = null;
		String fileInputName = "fileupload";

		String sourceFileName = uploadRequest.getFileName(fileInputName);

		if (Validator.isNotNull(sourceFileName)) {
			_log.debug("Uploading file: " + sourceFileName + " ...");

			String fileName = FileUtil.stripExtension(sourceFileName);
			_log.debug(fileName);

			appInput.setInputFileName(fileName);

			String extension = FileUtil.getExtension(sourceFileName);
			_log.debug(extension);

			// Get the uploaded file as a file.
			File uploadedFile = uploadRequest.getFile(fileInputName, true);
			File folder = new File(Constants.ROOT_FOLDER_NAME);
			// This is our final file path.
			file = new File(folder.getAbsolutePath() + Constants.FILE_SEPARATOR
					+ username + "_" + timestamp + "_" + fileName
					+ ((!extension.isEmpty()) ? "." + extension : ""));
			FileUtil.move(uploadedFile, file);

		}
		return file;
	}
}
