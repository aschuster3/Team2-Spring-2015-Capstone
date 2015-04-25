angular.module('mwl.calendar')
    .factory('sessionService', ['$http', function($http) {
        var urlBase = '/sessions-api';
        var service = {};

        service.getSessions = function () {
            return $http.get(urlBase);
        };

        service.getSession = function (id) {
            return $http.get(urlBase + '/' + id);
        };

        service.updateSession = function (session) {
            return $http.put(urlBase + '/' + session.id, session);
        };

        service.updateMultipleSessions = function (sessions) {
            return $http.put(urlBase + '/bulk/update', sessions);
        };

        service.deleteSession = function (id) {
            return $http.delete(urlBase + '/' + id);
        };

        service.createSession = function (session) {
            return $http.post(urlBase, session);
        };

        service.createRecurringSessions = function (session, recurringType, recurringEndDate) {
            return $http.post(urlBase + '/rec', {
                session: session,
                recurringSessionGroup: {
                    recurringType: recurringType,
                    endDate: recurringEndDate
                }
            });
        };

        service.createFromScheduleTemplate = function (scheduleTemplate, startDate, preventThawing) {
            return $http.post(urlBase + '/template/' + scheduleTemplate.uuid, {
                startDate: startDate,
                preventThawing: preventThawing
            });
        };

        service.removeRecurringSessions = function (session) {
            return $http.delete(urlBase + '/rec/' + session.id);
        };

        return service;
    }]);