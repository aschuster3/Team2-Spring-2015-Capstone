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

    $scope.calendarView = 'month';
    $scope.calendarDay = new Date();

    $scope.setCalendarToToday = function() {
      $scope.calendarDay = new Date();
    };

    $scope.showModalForCreate = function () {
      var defaultSession = {};
      showModal(defaultSession);
    };

    $scope.showModalForUpdate = function (event) {
      var copy = angular.copy(event);  // so we don't update real object until hitting "save"
      showModal(copy);
    };

    $scope.eventClicked = function(event) {
      console.log('eventClicked:  Clicking through icon not supported right now');
    };

    $scope.eventDeleted = function(event) {
      Sessions.delete(event);
    };


    /***********************************************************************
     * General Functions relevant to Grouping Sessions
     ***********************************************************************/
    function isCompleted(session) {
      var currentDate = new Date();
      return currentDate.getTime() - session.date.getTime() < 0;
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

          $scope.toggleDatePicker = function($event) {
            $event.preventDefault();
            $event.stopPropagation();

            $scope.showDatePicker = !$scope.showDatePicker;
          };


          $scope.clickDelete = function () {
            Sessions.delete($scope.event);
            $modalInstance.dismiss('cancel');
          };

          $scope.clickUpdate = function () {
            Sessions.update($scope.event);
            $modalInstance.dismiss('cancel');
          };

          $scope.clickCreate = function () {
            Sessions.create($scope.event);
            $modalInstance.dismiss('cancel');
          };

          $scope.clickCancel = function () {
            $modalInstance.dismiss('cancel');
          };

          /* form validation will not accept epoch time as valid Date */
          if (angular.isDefined(event.date) && !(event.date instanceof Date)) {
            event.date = new Date(event.date);
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