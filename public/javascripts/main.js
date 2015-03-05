'use strict';

/**
 * @ngdoc function
 * @name angularBootstrapCalendarApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the angularBootstrapCalendarApp
 */
angular.module('mwl.calendar')
  .controller('MainCtrl', function ($scope, $modal, moment, sessionService, learnerService) {

    var currentYear = moment().year();
    var currentMonth = moment().month();

    sessionService.getSessions()
        .success(function (data) {
          $scope.events = data;
        });

    $scope.deleteSession = function (sessionIndex) {
      var session = $scope.events[sessionIndex];
      sessionService.deleteSession(session.id)
          .success(function () {
            $scope.events.splice(sessionIndex, 1);
          })
    };

    $scope.createSession = function (sessionIndex) {
      console.log($scope.events);
      var session = $scope.events[sessionIndex];
      console.log(sessionIndex);
      console.log(session)
      sessionService.createSession(session)
          .success(function (data) {
            /* POST will return the created object
               The difference between the returned session
               and the session passed to the server is
               that the ID will now be assigned
             */
            $scope.events[sessionIndex] = data;
          });
    }

    $scope.updateSession = sessionService.updateSession;

    $scope.calendarView = 'month';
    $scope.calendarDay = new Date();

    function showModal(action, event) {
      $modal.open({
        templateUrl: 'modalContent.html',
        controller: function($scope, $modalInstance) {
          $scope.$modalInstance = $modalInstance;
          $scope.action = action;
          $scope.event = event;
        }
      });
    }

    $scope.eventClicked = function(event) {
      showModal('Clicked', event);
    };

    $scope.eventEdited = function(event) {
      showModal('Edited', event);
    };

    $scope.eventDeleted = function(event) {
      showModal('Deleted', event);
    };

    $scope.setCalendarToToday = function() {
      $scope.calendarDay = new Date();
    };

    $scope.toggle = function($event, field, event) {
      $event.preventDefault();
      $event.stopPropagation();

      event[field] = !event[field];
    };

  });