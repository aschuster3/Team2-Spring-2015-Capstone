angular.module('mwl.calendar')
    .factory('sessionService', ['$http', function($http) {
        var urlBase = '/sessions-api';
        var service = {};

        service.getSessions = function () {
            return $http.get(urlBase);
        };

        service.updateSession = function (session) {
            return $http.put(urlBase + '/' + session.id, session);
        };

        service.deleteSession = function (id) {
            return $http.delete(urlBase + '/' + id);
        };

        service.createSession = function (session) {
            return $http.post(urlBase, session);
        };

        service.createRecurringSessions = function (session, recurringType) {
            return $http.post(urlBase + '/rec', {
                session: session,
                recurringSessionGroup: {
                    recurringType: recurringType
                }
            })
        };

        service.removeRecurringSessions = function (session) {
            return $http.delete(urlBase + '/rec/' + session.id);
        };

        return service;
    }]);