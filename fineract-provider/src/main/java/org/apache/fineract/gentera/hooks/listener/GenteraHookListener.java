package org.apache.fineract.gentera.hooks.listener;

import org.apache.fineract.infrastructure.hooks.event.HookEvent;
import org.apache.fineract.infrastructure.hooks.event.HookEventSource;
import org.apache.fineract.infrastructure.hooks.listener.HookListener;
import org.apache.fineract.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class GenteraHookListener implements HookListener {
    private static final Logger logger = LoggerFactory.getLogger(GenteraHookListener.class);

    @PostConstruct
    public void init() {
        logger.warn(">>>>> GENTERA >>>>> YEAH!");
    }

    @Override
    public void onApplicationEvent(HookEvent event) {
        final String tenantIdentifier = event.getTenantIdentifier();

        final AppUser appUser = event.getAppUser();
        final String authToken = event.getAuthToken();

        final HookEventSource hookEventSource = event.getSource();
        final String entityName = hookEventSource.getEntityName();
        final String actionName = hookEventSource.getActionName();
        final String payload = event.getPayload();

        logger.warn(">>>>> GENTERA >>>>> {} - {} -> {}: {}", new Object[]{appUser.getUsername(), entityName, actionName, payload});

        // TODO: implement this

        /*
        - GLITGT (see: https://mifosforge.jira.com/wiki/display/MIFOSX/Loan+Tracking+strategies):
          - we could create one "umbrella" group loan (as datatable attached to group, multiline=multiple loans)
          - handle individual loans as joint group liability loan + parentLoanId (this is the ID from the datatable)
          - find out which loan product will we use?!? The three listed are actually one!
          - use datatable attached to individual loan to record the parent ID (ID of the datatable row that represents the GLITGT loan)
          - add listener: on individual loan create with parent ID, then check if total amount parent is still good, if not then increase accordingly
          - add listiner: on individual loan update (status), then query status aggregate and set it in group datatable
         */
    }
}
