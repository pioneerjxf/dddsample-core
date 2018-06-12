package se.citerus.dddsample.interfaces.handling;

import com.aggregator.HandlingReport;
import com.aggregator.HandlingReportErrors;
import com.aggregator.HandlingReportErrors_Exception;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import se.citerus.dddsample.application.ApplicationEvents;
import se.citerus.dddsample.domain.model.cargo.TrackingId;
import se.citerus.dddsample.domain.model.handling.HandlingEvent;
import se.citerus.dddsample.domain.model.location.UnLocode;
import se.citerus.dddsample.domain.model.voyage.VoyageNumber;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static se.citerus.dddsample.interfaces.handling.HandlingReportParser.parseCompletionTime;
import static se.citerus.dddsample.interfaces.handling.HandlingReportParser.parseEventType;
import static se.citerus.dddsample.interfaces.handling.HandlingReportParser.parseTrackingId;
import static se.citerus.dddsample.interfaces.handling.HandlingReportParser.parseUnLocode;
import static se.citerus.dddsample.interfaces.handling.HandlingReportParser.parseVoyageNumber;

/**
 * This web service endpoint implementation performs basic validation and parsing
 * of incoming data, and in case of a valid registration attempt, sends an asynchronous message
 * with the informtion to the handling event registration system for proper registration.
 *  
 */
@Controller
@RequestMapping("/handling")
public class HandlingReportController {

    private ApplicationEvents applicationEvents;
    private final static Log logger = LogFactory.getLog(HandlingReportController.class);

    public void submitReport(HandlingReport handlingReport) throws HandlingReportErrors_Exception {
        final List<String> errors = new ArrayList<String>();

        final Date completionTime = parseCompletionTime(handlingReport, errors);
        final VoyageNumber voyageNumber = parseVoyageNumber(handlingReport.getVoyageNumber(), errors);
        final HandlingEvent.Type type = parseEventType(handlingReport.getType(), errors);
        final UnLocode unLocode = parseUnLocode(handlingReport.getUnLocode(), errors);

        for (String trackingIdStr : handlingReport.getTrackingIds()) {
            final TrackingId trackingId = parseTrackingId(trackingIdStr, errors);

            if (errors.isEmpty()) {
                final Date registrationTime = new Date();
                final HandlingEventRegistrationAttempt attempt = new HandlingEventRegistrationAttempt(
                        registrationTime, completionTime, trackingId, voyageNumber, type, unLocode
                );

                applicationEvents.receivedHandlingEventRegistrationAttempt(attempt);
            } else {
                logger.error("Parse error in handling report: " + errors);
                final HandlingReportErrors faultInfo = new HandlingReportErrors();
                throw new HandlingReportErrors_Exception(errors.toString(), faultInfo);
            }
        }

    }

    private static final SimpleDateFormat TIME_PATTERN = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public void submitReportV2(@RequestParam(value = "time") String time,
                               @RequestParam(value = "trackingIds") List<String> trackingIds,
                               @RequestParam(value = "type") String typeInput,
                               @RequestParam(value = "unLocode") String unLocodeInput,
                               @RequestParam(value = "voyageNumber") String voyageNumberInput) throws ParseException {
        final List<String> errors = new ArrayList<String>();

        final Date completionTime = TIME_PATTERN.parse(time);
        final VoyageNumber voyageNumber = parseVoyageNumber(voyageNumberInput, errors);
        final HandlingEvent.Type type = parseEventType(typeInput, errors);
        final UnLocode unLocode = parseUnLocode(unLocodeInput, errors);

        return;
    }


  public void setApplicationEvents(ApplicationEvents applicationEvents) {
    this.applicationEvents = applicationEvents;
  }

}
