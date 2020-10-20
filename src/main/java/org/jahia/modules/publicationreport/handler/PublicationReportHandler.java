package org.jahia.modules.publicationreport.handler;

import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.helper.PublicationHelper;
import org.jahia.modules.publicationreport.model.EnvironmentInfo;
import org.jahia.modules.publicationreport.ResultMessage;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.webflow.execution.RequestContext;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 *    
 * Class responsible to run the report and export results to webflow
 */
public class PublicationReportHandler {

    private static final Logger logger = LoggerFactory.getLogger(PublicationReportHandler.class);
    private List<ResultMessage> resultReport = new ArrayList<>();
    private StringBuilder errorMessage = new StringBuilder();

    private static String author = "";
    private static String startPath = "";
    private static String siteKey = "";
    private static Set<String> lang;

    /**
     * Set the error message to be shown in UI
     *
     * @param message Error message
     */
    public void setErrorMessage(String message) {
        this.errorMessage.append("</br>" + message);
    }

    public void checkPublicationState(JCRNodeWrapper node, JCRSessionWrapper session) throws GWTJahiaServiceException, RepositoryException {

        JCRSessionFactory.getInstance().setCurrentUser(org.jahia.registries.ServicesRegistry.getInstance().getJahiaUserManagerService().lookupRootUser().getJahiaUser());
        JCRSessionFactory.getInstance().setCurrentLocale(Locale.ENGLISH);
        PublicationHelper publicationHelper = (PublicationHelper) SpringContextSingleton.getBean("PublicationHelper");
        List<String> uuids = new ArrayList<String>();
        uuids.add(node.getIdentifier());

        List<GWTJahiaPublicationInfo> infos = publicationHelper.getFullPublicationInfos(uuids, lang, JCRSessionFactory.getInstance().getCurrentUserSession("default", Locale.ENGLISH), false, false);

        for (GWTJahiaPublicationInfo info : infos) {
            String modifiedBy = node.getPropertyAsString("jcr:lastModifiedBy");
            String createdBy = node.getPropertyAsString("jcr:createdBy");

            if (info.getStatus() != PublicationInfo.NOT_PUBLISHED) {
                continue;
            }

            if (createdBy == null) {
                continue;
            }

            if (author.isEmpty() || (modifiedBy != null && modifiedBy.trim().equals(author.trim()))) {

                String nodePath = node.getPath();
                String uuid = node.getPropertyAsString("jcr:uuid");
                String publishStatus = "NOT_PUBLISHED";
                String lastModified = node.getPropertyAsString("jcr:lastModified");

                try {
                    int lastIndex = this.resultReport.size() - 1;
                    ResultMessage previousResult = this.resultReport.get(lastIndex);

                    if (previousResult.getUuid().equals(uuid)) {
                        String[] previousLocale = previousResult.getLocale().split(",");
                        if (Arrays.asList(previousLocale).contains(info.getLanguage())) {
                            continue;
                        }
                        String localeList = previousResult.getLocale() + "," + info.getLanguage();
                        previousResult.setLocale(localeList);
                        this.resultReport.set(lastIndex, previousResult);
                    } else {
                        ResultMessage resultMessage = new ResultMessage(nodePath, publishStatus, modifiedBy, lastModified, info.getLanguage(), uuid);
                        this.resultReport.add(resultMessage);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    ResultMessage resultMessage = new ResultMessage(nodePath, publishStatus, modifiedBy, lastModified, info.getLanguage(), uuid);
                    this.resultReport.add(resultMessage);
                }
            }

        }
        JCRNodeIteratorWrapper nodes = node.getNodes();
        while (nodes.hasNext()) {
            JCRNodeWrapper subNode = (JCRNodeWrapper) nodes.next();
            checkPublicationState(subNode, session);
        }

    }


    /**
     *     
     * Execute the migration
     *
     * @param environmentInfo Object containing environment information read from frontend
     * @param context         Page context
     * @return true if OK; otherwise false
     */
    public Boolean execute(final EnvironmentInfo environmentInfo,
                           RequestContext context) throws RepositoryException {

        context.getMessageContext().clearMessages();
        resultReport.clear();

        this.author = environmentInfo.getAuthor();
        this.startPath = environmentInfo.getStartPath();

        if (startPath.startsWith("/sites/")) {
            String[] pathSplit = startPath.split("/");
            this.siteKey = pathSplit[2];
        }

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
                try {
                    JCRNodeWrapper start = session.getNode(startPath);

                    if (this.siteKey.isEmpty() == false) {
                        JCRNodeWrapper siteKeyNode = session.getNode("/sites/" + this.siteKey);

                        this.lang = ((JCRSiteNode) siteKeyNode).getActiveLiveLanguages();
                    }

                    checkPublicationState(start, session);

                } catch (PathNotFoundException e) {
                    setErrorMessage("Path not found!");
                    return false;
                } catch (GWTJahiaServiceException e) {
                    setErrorMessage("Error with PublicationHelper service");
                    return false;
                }

                return true;
            });
        } catch (RepositoryException e) {
            setErrorMessage("Repository Error!");
            return false;
        }

        if (this.errorMessage.length() > 0) {
            context.getMessageContext().addMessage(new MessageBuilder().error()
                    .defaultText("An error encountered: " + this.errorMessage).build());
            return false;
        } else {
            context.getFlowScope().put("migrationReport", this.resultReport);
        }

        logger.info("Finishing modules report");

        return true;
    }
}
