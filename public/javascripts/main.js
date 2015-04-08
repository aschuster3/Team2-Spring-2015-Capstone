'use strict';

/**
 * @ngdoc function
 * @name angularBootstrapCalendarApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the angularBootstrapCalendarApp
 */
angular.module('mwl.calendar')
  .controller('MainCtrl', function ($scope, $modal, moment, Sessions, learnerService) {

    /***********************************************************************
     * CALENDAR CODE
     ***********************************************************************/

    $scope.events = Sessions.sessions;

    var currentYear = moment().year();
    var currentMonth = moment().month();

    /* from RecurringSessionGroup.java */
    var REC_TYPE_NONE = 0;
    var REC_TYPE_WEEKLY = 1;

    $scope.calendarView = 'month';
    $scope.calendarDay = new Date();

    $scope.setCalendarToToday = function() {
      $scope.calendarDay = new Date();
    };

    $scope.showModalForCreate = function () {
      var defaultSession = {
        /* the modal's checkboxes do not use ng-model like other inputs.

         * So, initialize these properties before passing to modal
         * to avoid undefined exceptions
         */
        supportedLearnerTypes: [],
        supportedLearnerTypesAsString: ''
      };
      showModal(defaultSession);
    };

    $scope.showModalForUpdate = function (event) {
      var copy = angular.copy(event);  // so we don't update real object until hitting "save"
      showModal(copy);
    };

    $scope.eventClicked = function(event) {
      console.log('eventClicked:  Clicking through icon not supported right now');
    };

    $scope.eventDeleted = deleteEvent;
    
    /***********************************************************************
     * General Functions relevant to adding Sessions to Learners
     ***********************************************************************/
    $scope.currentLearner = "error";
    
    $scope.$watch('learnerDropdown', function(learnerJSONString) {
    	if (!(typeof learnerJSONString === "undefined")) {
        $scope.currentLearner = JSON.parse(learnerJSONString);
        updateEventTypes($scope.events, $scope.currentLearner);
    	}
    });
    
    $scope.addSessionToLearner = function(session) {
    	if(session.assignedLearner == null && $scope.currentLearner != "error") {
	    	session.assignedLearner = $scope.currentLearner.email;
	    	session.type = "invalid";
	      Sessions.update(session);
    	}
    };
    
    function updateEventTypes(events, currentLearner) {
      events.forEach(function (event) {
        //console.log(event);
        if (event.assignedLearner !== null) {
          //console.log(event.title + " is being flagged as INVALID (1)");
          event.type = "invalid";
        } else if (typeof currentLearner !== "object") {
          //console.log(event.title + " is being flagged as info (2)");
          event.type = "info";
        } else if (event.supportedLearnerTypes.indexOf(currentLearner.learnerType) === -1) {
          //console.log(event.title + " is being flagged as INVALID (3)");
          event.type = "invalid";
        } else {
          //console.log(event.title + " is being flagged as info (4)");
          event.type = "info";
        }
      });
    }

    // TODO Refactor this for better separation of Admin and Coordinator!!!!
    var isAdminView = document.querySelector("#student-form") === null;
    $scope.isAdminView = isAdminView;
    if (!isAdminView) {
      // temporary hack to control icon via editable attribute
      // only when in coordinator view
      $scope.$watch('currentLearner', function() {
        if (typeof $scope.currentLearner === "object") {
          $scope.events.forEach(function (event) {
            event.editable = event.type === "info";
          });
        } else {
          $scope.events.forEach(function (event) {
            event.editable = false;
          });
        }
      });
    }

    $scope.displayEditIcon = function (event) {
      if ($scope.isAdminView) {
        return true;
      } else if (typeof $scope.assignedLearner !== "object") {
        return false;
      } else {
        return event.type !== "invalid";
      }
    };

    /***********************************************************************
     * General Functions relevant to Grouping or Deleting Sessions
     ***********************************************************************/
    function isCompleted(session) {
      var currentDate = new Date();
      return currentDate.getTime() - session.date.getTime() < 0;
    }

    function deleteEvent(event) {
      // TODO provide option to user
      if (event.recurringGroupId === null) {
        Sessions.delete(event);
      } else {
        Sessions.deleteRecurringGroup(event);
      }
    }

    /***********************************************************************
     * General Functions for Create/Edit Session
     ***********************************************************************/
    var AM_START_HOURS = 8;
    var AM_END_HOURS = 12;
    var PM_START_HOURS = 13;
    var PM_END_HOURS = 17;

    /*
     * starts_at and ends_at are required properties for the calendar
     *
     * Logically, we track events by
     *   1) Date
     *   2) AM/PM
     *
     * So, update starts_at and ends_at whenever these two properties change
     * ($scope.$watch is overkill for this)
     */
    function updateStartsAtEndsAt(session) {
      if (!angular.isDefined(session.date)) {
        return;
      }

      // these will be Date objects
      var starts_at;
      var ends_at;

      if (session.date instanceof Date) {
        starts_at = angular.copy(session.date);
        ends_at = angular.copy(session.date);
      } else {
        // session.date is just milliseconds
        starts_at = new Date(session.date);
        ends_at = new Date(session.date);
      }

      if (session.isAM) {
        starts_at.setHours(AM_START_HOURS);
        ends_at.setHours(AM_END_HOURS);
      } else {
        starts_at.setHours(PM_START_HOURS);
        ends_at.setHours(PM_END_HOURS);
      }

      session.starts_at = starts_at.getTime();
      session.ends_at = ends_at.getTime();
    }

    /***********************************************************************
     * Session Creation/Editing via Modal
     ***********************************************************************/


    function showModal(event) {
      $modal.open({
        templateUrl: 'modalContent.html',
        controller: function modalController($scope, $modalInstance, Sessions) {
          $scope.clinicOptions = [
            'Emory Clinic',
            'Grady Clinic',
            'VA Clinic',
            'VA Telederm',
            'Emory Dermpath'
          ];

          $scope.$modalInstance = $modalInstance;
          $scope.event = event;
          $scope.isCreateModal = !angular.isDefined(event.id);
          $scope.modalTitle = $scope.isCreateModal ? 'Create Event' : 'Edit Event';

          $scope.updateStartsAtEndsAt = updateStartsAtEndsAt;

          $scope.showDatePicker = false;


          $scope.REC_TYPE_NONE = REC_TYPE_NONE;
          $scope.REC_TYPE_WEEKLY = REC_TYPE_WEEKLY;
          $scope.event.recurringType = REC_TYPE_NONE;

          $scope.toggleDatePicker = function($event) {
            $event.preventDefault();
            $event.stopPropagation();

            $scope.showDatePicker = !$scope.showDatePicker;
          };


          $scope.clickDelete = function () {
            // TODO provide option to delete recurring session group
            deleteEvent($scope.event);
            $modalInstance.dismiss('cancel');
          };

          $scope.clickUpdate = function () {
            Sessions.update($scope.event);
            $modalInstance.dismiss('cancel');
          };

          $scope.clickCreate = function () {
            if ($scope.event.recurringType !== REC_TYPE_NONE) {
              Sessions.createRecurringGroup($scope.event, $scope.event.recurringType);
            } else {
              Sessions.create($scope.event);
            }
            $modalInstance.dismiss('cancel');
          };

          $scope.clickCancel = function () {
            $modalInstance.dismiss('cancel');
          };

          /* form validation will not accept epoch time as valid Date */
          if (angular.isDefined(event.date) && !(event.date instanceof Date)) {
            event.date = new Date(event.date);
          }


          /* FOR LEARNER TYPES */

          // currently:  from Learner.LEARNER_TYPES
          $scope.allLearnerTypes = [
            "Sub-I",
            "Sub-I Medical Student",
            "Ambulatory Medical Student",
            "Dermatology Resident",
            "Pediatrics Resident",
            "Emory Internal Medicine",
            "Morehouse Internal Medicine",
            "Family Medicine",
            "Podiatry Resident",
            "Geriatrics Resident",
            "Rheumatology Resident",
            "Nurse Practitioner Student",
            "Physician Assistant Student",
            "Pediatrics Allergy Fellow",
            "International Student",
            "Pre-Med Student"
          ];

          $scope.toggleLearnerTypeChecked = toggleLearnerTypeChecked;
          $scope.learnerTypeIsChecked = learnerTypeIsChecked;

          function toggleLearnerTypeChecked(event, learnerType) {
            var index = event.supportedLearnerTypes.indexOf(learnerType);

            if (index === -1) {
              event.supportedLearnerTypes.push(learnerType);
            } else {
              event.supportedLearnerTypes.splice(index, 1);
            }

            event.supportedLearnerTypesAsString = event.supportedLearnerTypes.join(',');
          }

          function learnerTypeIsChecked(event, learnerType) {
            return event.supportedLearnerTypes.indexOf(learnerType) !== -1;
          }
        }
      });
    }

    /***********************************************************************
     * Modal for creating a session in a template -- HElP!
     ***********************************************************************/

    function showSessionModal(event) {
      $modal.open({
        templateUrl: 'sessionModal.html',
        controller: function modalController($scope, $modalInstance, Sessions) {
          $scope.clinicOptions = [
            'Emory Clinic',
            'Grady Clinic',
            'VA Clinic',
            'VA Telederm',
            'Emory Dermpath'
          ];

          $scope.$modalInstance = $modalInstance;
          $scope.event = event;
          $scope.isCreateModal = !angular.isDefined(event.id);
          $scope.modalTitle = $scope.isCreateModal ? 'Create Event' : 'Edit Event';

          $scope.updateStartsAtEndsAt = updateStartsAtEndsAt;

          $scope.showDatePicker = false;


          $scope.REC_TYPE_NONE = REC_TYPE_NONE;
          $scope.REC_TYPE_WEEKLY = REC_TYPE_WEEKLY;
          $scope.event.recurringType = REC_TYPE_NONE;

          $scope.toggleDatePicker = function ($event) {
            $event.preventDefault();
            $event.stopPropagation();

            $scope.showDatePicker = !$scope.showDatePicker;
          };


          $scope.clickDelete = function () {
            // TODO provide option to delete recurring session group
            deleteEvent($scope.event);
            $modalInstance.dismiss('cancel');
          };

          $scope.clickUpdate = function () {
            Sessions.update($scope.event);
            $modalInstance.dismiss('cancel');
          };

          $scope.clickCreate = function () {
            if ($scope.event.recurringType !== REC_TYPE_NONE) {
              Sessions.createRecurringGroup($scope.event, $scope.event.recurringType);
            } else {
              Sessions.create($scope.event);
            }
            $modalInstance.dismiss('cancel');
          };

          $scope.clickCancel = function () {
            $modalInstance.dismiss('cancel');
          };

          /* form validation will not accept epoch time as valid Date */
          if (angular.isDefined(event.date) && !(event.date instanceof Date)) {
            event.date = new Date(event.date);
          }


          /* FOR LEARNER TYPES */

          // currently:  from Learner.LEARNER_TYPES
          $scope.allLearnerTypes = [
            "Sub-I",
            "Sub-I Medical Student",
            "Ambulatory Medical Student",
            "Dermatology Resident",
            "Pediatrics Resident",
            "Emory Internal Medicine",
            "Morehouse Internal Medicine",
            "Family Medicine",
            "Podiatry Resident",
            "Geriatrics Resident",
            "Rheumatology Resident",
            "Nurse Practitioner Student",
            "Physician Assistant Student",
            "Pediatrics Allergy Fellow",
            "International Student",
            "Pre-Med Student"
          ];

          $scope.toggleLearnerTypeChecked = toggleLearnerTypeChecked;
          $scope.learnerTypeIsChecked = learnerTypeIsChecked;

          function toggleLearnerTypeChecked(event, learnerType) {
            var index = event.supportedLearnerTypes.indexOf(learnerType);

            if (index === -1) {
              event.supportedLearnerTypes.push(learnerType);
            } else {
              event.supportedLearnerTypes.splice(index, 1);
            }

            event.supportedLearnerTypesAsString = event.supportedLearnerTypes.join(',');
          }

          function learnerTypeIsChecked(event, learnerType) {
            return event.supportedLearnerTypes.indexOf(learnerType) !== -1;
          }
        }
      });
    }
  })


  .directive('datepickerPopup', ['datepickerPopupConfig', 'dateParser', 'dateFilter', function (datepickerPopupConfig, dateParser, dateFilter) {
    /*
     * Taken from https://github.com/angular-ui/bootstrap/issues/2659
     *
     * Issue:  In the modal, when session.date (ng-model for datepicker) is defined
     * as a Date object, the initial displayed text is not formatted correctly.
     */
    return {
      'restrict': 'A',
      'require': '^ngModel',
      'link': function ($scope, element, attrs, ngModel) {
        var dateFormat;

        //*** Temp fix for Angular 1.3 support [#2659](https://github.com/angular-ui/bootstrap/issues/2659)
        attrs.$observe('datepickerPopup', function(value) {
          dateFormat = value || datepickerPopupConfig.datepickerPopup;
          ngModel.$render();
        });

        ngModel.$formatters.push(function (value) {
          return ngModel.$isEmpty(value) ? value : dateFilter(value, dateFormat);
        });
      }
    };
  }]);