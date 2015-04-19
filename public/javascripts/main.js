'use strict';

/**
 * @ngdoc function
 * @name angularBootstrapCalendarApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the angularBootstrapCalendarApp
 */
angular.module('mwl.calendar')
  .controller('MainCtrl', function ($scope, $modal, moment, Sessions, learnerService, notificationModalService) {

    /***********************************************************************
     * CALENDAR CODE
     ***********************************************************************/

    $scope.events = Sessions.sessions;

    var currentYear = moment().year();
    var currentMonth = moment().month();

    /* from RecurringSessionGroup.java */
    var REC_TYPE_NONE = 0;
    var REC_TYPE_WEEKLY = 1;
    var REC_TYPE_MONTHLY = 2;

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
     * For adding schedule templates to the calendar.
     ***********************************************************************/
    $scope.showAddTemplateModal = function () {
      $modal.open({
        templateUrl: '/assets/templates/addTemplateModalTemplate.html',
        controller: 'AddTemplateModalCtrl'
      });
    };


    /***********************************************************************
     * General Functions relevant to adding Sessions to Learners
     ***********************************************************************/
    $scope.currentLearner = "error";

    function learnerIsSelected () {
      return typeof $scope.currentLearner === 'object';
    }

    
    $scope.$watch('learnerDropdown', function(learnerJSONString) {
    	if (!(typeof learnerJSONString === "undefined")) {
        $scope.currentLearner = JSON.parse(learnerJSONString);
        updateEventTypes($scope.events, $scope.currentLearner);
    	}
    });
    
    $scope.addSessionToLearner = function(session) {
      var sessionsToAssign;
      if (belongsToSchedule(session)) {
        sessionsToAssign = allSessionsInSameSchedule(session);
      } else {
        sessionsToAssign = [session];
      }

      /*
       * We send a single update AJAX call, which will tell us whether
       * or not the group is taken.
       *
       * If the group is free, then send AJAX calls for remaining sessions in group.
       */

      var sessionWithNewLearner = angular.copy(session);
    	if(sessionWithNewLearner.assignedLearner == null && learnerIsSelected()) {
        var selectedLearner = angular.copy($scope.currentLearner);
	    	assignSingleSessionToLearner(sessionWithNewLearner, selectedLearner);
	      Sessions.update(sessionWithNewLearner).then(
          function success() {
            sessionsToAssign.forEach(function (elem) {
              var updatedSession = angular.copy(elem);
              assignSingleSessionToLearner(updatedSession, selectedLearner);
              Sessions.update(updatedSession);
            });
          },
          function error() {
            notificationModalService.show("Error: Another learner has been signed up for this session already!");
            Sessions.refresh(session);
          }
        );
    	}
    };

    $scope.removeSessionFromLearner = function (session) {
      if (session.assignedLearner == null || !learnerIsSelected()) {
        return;
      }

      if (session.assignedLearner !== $scope.currentLearner.email) {
        return;
      }

      var sessionsToRemoveLearner;
      if (belongsToSchedule(session)) {
        sessionsToRemoveLearner = allSessionsInSameSchedule(session);
      } else {
        sessionsToRemoveLearner = [session];
      }

      sessionsToRemoveLearner.forEach(function (elem) {
        var sessionWithoutLearner = angular.copy(elem);
        removeSingleSessionFromLearner(sessionWithoutLearner);
        Sessions.update(sessionWithoutLearner);
      });
    };

    function assignSingleSessionToLearner(session, learner) {
      session.assignedLearner = learner.email;
      session.type = "invalid";
      session.editable = false;
      session.deletable = true;
    }

    function removeSingleSessionFromLearner(session) {
      session.assignedLearner = null;
      session.type = "info";
      session.editable = true;
      session.deletable = false;
    }
    
    function updateEventTypes(events, currentLearner) {
      events.forEach(function (event) {
        //console.log(event);
        if (event.assignedLearner !== null) {
          //console.log(event.title + " is being flagged as INVALID (1)");
          event.type = "invalid";
        } else if (typeof currentLearner !== "object") {
          //console.log(event.title + " is being flagged as info (2)");
          event.type = "info";
        } else if (event.supportsAnyLearnerType) {
          event.type = "info";
        } else if (event.supportedLearnerTypes.indexOf(currentLearner.learnerType) === -1) {
          //console.log(event.title + " is being flagged as INVALID (4)");
          event.type = "invalid";
        } else {
          //console.log(event.title + " is being flagged as info (5)");
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
            event.deletable = event.type === "invalid" && event.assignedLearner === $scope.currentLearner.email;
          });
        } else {
          $scope.events.forEach(function (event) {
            event.editable = false;
            event.deletable = false;
          });
        }
      });
    }

    $scope.displayEditIcon = function (event) {
      if ($scope.isAdminView) {
        return true;
      } else if (!learnerIsSelected()) {
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

    function belongsToSchedule(session) {
      return session.scheduleGroupId !== null;
    }

    function allSessionsInSameSchedule(session) {
      if (session.scheduleGroupId === null) {
        return [];
      }

      var sessionsForSchedule = [];
      $scope.events.forEach(function (otherSession) {
        if (session.scheduleGroupId === otherSession.scheduleGroupId) {
          sessionsForSchedule.push(otherSession);
        }
      });
      return sessionsForSchedule;
    }

    function deleteEvent(event) {
      Sessions.delete(event).then(
        function success() {
          if (event.assignedLearner !== null) {
            notificationModalService.show("An email has been sent to notify the assigned learner and their coordinator that this clinic was cancelled.", "Filled Clinic Was Deleted");
          }
        }
      );
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

    $scope.showMessageModal = showMessageModal;
    function showMessageModal(message, title) {
      notificationModalService.show(message, title);
    }

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
          $scope.modalTitle = $scope.isCreateModal ? 'Create Half Day Session' : 'Edit Half Day Session';

          $scope.updateStartsAtEndsAt = updateStartsAtEndsAt;

          $scope.showDatePicker = false;


          $scope.REC_TYPE_NONE = REC_TYPE_NONE;
          $scope.REC_TYPE_WEEKLY = REC_TYPE_WEEKLY;
          $scope.REC_TYPE_MONTHLY = REC_TYPE_MONTHLY;
          $scope.event.recurringType = REC_TYPE_NONE;

          $scope.toggleDatePicker = function ($event) {
            stopEventAndToggleProperty($event, 'showDatePicker');
          };

          $scope.toggleRecurringEndDatePicker = function($event) {
            stopEventAndToggleProperty($event, 'showRecurringEndDatePicker');
          };

          $scope.isRecurringEvent = function (event) {
            return event.recurringType !== REC_TYPE_NONE;
          };

          $scope.isValidRecurringEndDate = function (date) {
            if (!($scope.event.date instanceof Date) || !(date instanceof Date)) {
              return false;
            }

            if ($scope.event.recurringType === REC_TYPE_WEEKLY) {
              return $scope.event.date.getDay() === date.getDay();
            } else if ($scope.event.recurringType === REC_TYPE_MONTHLY) {
              return isValidRecurringMonthEndDate(date);
            } else {
              return false;
            }

          };

          /*
           * For now, the least complicated implementation that
           * also minimizes confusion is only allowing user
           * to select the last day of the month.
           */
          function isValidRecurringMonthEndDate(date) {
            var lastDayOfCurrentMonth = moment(date).endOf('month');
            var currentDay = moment(date);

            return currentDay.dayOfYear() === lastDayOfCurrentMonth.dayOfYear();
          }

          $scope.$watch('event.date', refreshDatepicker);
          $scope.$watch('event.recurringType', refreshDatepicker);

          function refreshDatepicker() {
            if (!$scope.isValidRecurringEndDate($scope.event.recurringEndDate)) {
              $scope.event.recurringEndDate = null;
            }
            $scope.$broadcast('refreshDatepickers');
          }

          function stopEventAndToggleProperty($event, scopePropertyName) {
            $event.preventDefault();
            $event.stopPropagation();

            $scope[scopePropertyName] = !$scope[scopePropertyName];
          }

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
              Sessions.createRecurringGroup($scope.event, $scope.event.recurringType, $scope.event.recurringEndDate);
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

          var learnerTypeRegex = /^\w[^,]*$/;
          function isValidLearnerType (learnerType) {
            return learnerTypeRegex.test(learnerType);
          }

          event.supportedLearnerTypes.forEach(function (learnerType) {
            if ($scope.allLearnerTypes.indexOf(learnerType) === -1
                  && isValidLearnerType(learnerType)) {
              $scope.allLearnerTypes.push(learnerType);
            }
          });

          $scope.toggleLearnerTypeChecked = toggleLearnerTypeChecked;
          $scope.toggleAnyLearnerTypeChecked = toggleAnyLearnerTypeChecked;
          $scope.learnerTypeIsChecked = learnerTypeIsChecked;
          $scope.clickShowOtherLearnerInput = showOtherLearnerInput;
          $scope.clickAddOtherLearnerType = addOtherLearnerType;
          $scope.newOtherLearnerType = "";
          $scope.showOtherLearnerInput = false;
          $scope.isValidLearnerType = isValidLearnerType;

          $scope.$watch('event.supportedLearnerTypes', function (newVal) {
            $scope.event.supportedLearnerTypesAsString = $scope.event.supportedLearnerTypes.join(',');
          }, true);

          function toggleLearnerTypeChecked(event, learnerType) {
            var index = event.supportedLearnerTypes.indexOf(learnerType);

            if (index === -1) {
              event.supportedLearnerTypes.push(learnerType);
            } else {
              event.supportsAnyLearnerType = false;
              event.supportedLearnerTypes.splice(index, 1);
            }
          }

          function toggleAnyLearnerTypeChecked(event) {
            if (event.supportsAnyLearnerType) {
              clearAllSupportedLearnerTypes(event);
            } else {
              setAllSupportedLearnerTypes(event);
            }
          }

          function clearAllSupportedLearnerTypes(event) {
            event.supportsAnyLearnerType = false;
            event.supportedLearnerTypes = [];
          }

          function setAllSupportedLearnerTypes(event) {
            event.supportsAnyLearnerType = true;
            event.supportedLearnerTypes = [];
            $scope.allLearnerTypes.forEach(function (learnerType) {
              event.supportedLearnerTypes.push(learnerType);
            });
          }

          function learnerTypeIsChecked(event, learnerType) {
            return event.supportedLearnerTypes.indexOf(learnerType) !== -1;
          }

          function showOtherLearnerInput() {
            $scope.newOtherLearnerInput = "";
            $scope.showOtherLearnerInput = true;
          }

          function addOtherLearnerType() {
            var newLearnerType = angular.copy($scope.newOtherLearnerType);
            $scope.allLearnerTypes.push(newLearnerType);
            $scope.newOtherLearnerType = "";
            $scope.showOtherLearnerInput = false;

            if ($scope.event.supportsAnyLearnerType) {
              $scope.event.supportedLearnerTypes.push(newLearnerType);
            }
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