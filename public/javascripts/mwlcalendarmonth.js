'use strict';

/**
 * @ngdoc directive
 * @name angularBootstrapCalendarApp.directive:mwlCalendarMonth
 * @description
 * # mwlCalendarMonth
 */
angular.module('mwl.calendar')
  .directive('mwlCalendarMonth', function ($sce, $timeout, $filter, moment, calendarHelper) {
    return {
      templateUrl: 'assets/templates/month.html',
      restrict: 'EA',
      require: '^mwlCalendar',
      scope: {
        events: '=calendarEvents',
        currentDay: '=calendarCurrentDay',
        eventClick: '=calendarEventClick',
        eventEditClick: '=calendarEditEventClick',
        eventDeleteClick: '=calendarDeleteEventClick',
        editEventHtml: '=calendarEditEventHtml',
        deleteEventHtml: '=calendarDeleteEventHtml',
        autoOpen: '=calendarAutoOpen',
        useIsoWeek: '=calendarUseIsoWeek',
        displayEditIcon: '=',
        isAdminView: '='
      },
      link: function postLink(scope, element, attrs, calendarCtrl) {

        scope.eventFontColor = function (event) {
          if (event.type === "invalid") {
            return {"color":"#999999"};
          } else {
            return {"color":"white"};
          }
        }

        var firstRun = false;

        scope.$sce = $sce;

        calendarCtrl.titleFunctions.month = function(currentDay) {
          return $filter('date')(currentDay, 'MMMM yyyy');
        };

        function updateView() {
          var oldView = scope.view;  // scope.view may not be initialized yet!
          scope.view = calendarHelper.getMonthView(scope.events, scope.currentDay, scope.useIsoWeek);

          /*
           * This code is not originally here!
           *
           * We don't want the box to collapse if an event is edited.
           * So, reassign all isOpened properties to their original state to achieve this.
           *
           */
          if (oldView) {
            var openWeekIndex = -1;
            var openDayIndex = -1;
            scope.view.forEach(function (week, weekIndex) {
              week.isOpened = !!oldView[weekIndex].isOpened;
              if (week.isOpened) {
                openWeekIndex = weekIndex;
              }

              week.forEach(function (day, dayIndex) {
                day.isOpened = !!oldView[weekIndex][dayIndex].isOpened;
                if (day.isOpened) {
                  openDayIndex = dayIndex;
                }
              });
            });

            /*
             * openEvents does not appear to pickup changes immediately from
             * the main events array (prototype issue? child scope issue?)
             *
             * So, reset it here to pickup the changes.
             */
            if (openWeekIndex !== -1 && openDayIndex !== -1) {
              scope.openEvents = scope.view[openWeekIndex][openDayIndex].events;
            }
          }

          //Auto open the calendar to the current day if set
          if (scope.autoOpen && !firstRun) {
            scope.view.forEach(function(week, rowIndex) {
              week.forEach(function(day, cellIndex) {
                if (day.inMonth && moment(scope.currentDay).startOf('day').isSame(day.date.startOf('day'))) {
                  console.log("I am a moron who is clicking... " + rowIndex + "|" + cellIndex);
                  scope.dayClicked(rowIndex, cellIndex);
                  $timeout(function() {
                    firstRun = false;
                  });
                }
              });
            });
          }

        }

        scope.$watch('currentDay', updateView);
        scope.$watch('events', updateView, true);

        scope.weekDays = calendarHelper.getWeekDayNames(false, scope.useIsoWeek);

        scope.dayClicked = function(rowIndex, cellIndex) {

          var handler = calendarHelper.toggleEventBreakdown(scope.view, rowIndex, cellIndex);
          scope.view = handler.view;
          scope.openEvents = handler.openEvents;

        };

        scope.drillDown = function(day) {
          calendarCtrl.changeView('day', moment(scope.currentDay).clone().date(day).toDate());
        };

        scope.highlightEvent = function(event, shouldAddClass) {

          scope.view = scope.view.map(function(week) {

            week.isOpened = false;

            return week.map(function(day) {

              delete day.highlightClass;
              day.isOpened = false;

              if (shouldAddClass) {
                var dayContainsEvent = day.events.filter(function(e) {
                  return e.$id == event.$id;
                }).length > 0;

                if (dayContainsEvent) {
                  day.highlightClass = 'day-highlight dh-event-' + event.type;
                }
              }

              return day;

            });

          });

        };

      }
    };
  });
