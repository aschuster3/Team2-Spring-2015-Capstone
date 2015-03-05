angular.module('mwl.calendar')
    .factory('sessionService', ['$http', function($http) {
        var urlBase = '/sessions-api';
        var service = {};

        var extractLearnerFields = function (obj) {
            if (obj === null) {
                return null;
            }
            /* taken from models.Learner */
            return {
                id: obj.id,
                firstName: obj.firstName,
                lastName: obj.lastName,
                ownerEmail: obj.ownerEmail
            }
        }

        /* Plays JSON parser does not like it when it receives JSON representing
         a model object when the JSON contains extraneous properties.

         So, this "filters" Javascript session objects before sending them
         to the server, removing extra fields that might be on the object.
         */
        var extractSessionFields = function (obj) {
            /* taken from models.Session */
            return {
                id: obj.id,
                title: obj.title,
                starts_at: obj.starts_at,
                ends_at: obj.ends_at,
                isFree: obj.isFree,
                type: obj.type,
                learner: extractLearnerFields(obj.learner)
            }
        }

        service.getSessions = function () {
            return $http.get(urlBase);
        };

        service.updateSession = function (session) {
            return $http.put(urlBase + '/' + session.id, extractSessionFields(session));
        };

        service.deleteSession = function (id) {
            return $http.delete(urlBase + '/' + id);
        };

        service.createSession = function (session) {
            return $http.post(urlBase, extractSessionFields(session));
        }

        return service;
    }]);