angular.module('mwl.calendar')
  .factory('Sessions', ['sessionService', '$log', function(sessionService, $log) {
    var Sessions = {
      sessions: [],
      refreshAll: getAll,
      refresh: get,
      create: create,
      createRecurringGroup: createRecurringGroup,
      createFromScheduleTemplate: createFromScheduleTemplate,
      update: update,
      delete: remove,
      deleteRecurringGroup: removeRecurringGroup
    };
    getAll();
    return Sessions;



    function findSessionIndexWithId(id) {
      for (var i = 0; i < Sessions.sessions.length; i++) {
        if (Sessions.sessions[i].id === id) {
          return i;
        }
      }
      return -1;
    }

    function getAll() {
      return sessionService.getSessions()
        .then(function success(response) {
          var data = response.data;
          Sessions.sessions.splice(0, Sessions.sessions.length);
          angular.forEach(data, function (session) {
            Sessions.sessions.push(session);
          });
        }, function error(errorResponse) {
          $log.error('sessionService.getSessions failed: ', errorResponse);
          throw errorResponse;
        });
    }

    function get(session) {
      return sessionService.getSession(session.id)
        .then(function success(response) {
          var data = response.data;
          var index = findSessionIndexWithId(session.id);
          Sessions.sessions[index] = data;
        }, function error(response) {
          $log.error('sessionService.get failed: ', response);
          throw response;
        })
    }

    function create(newSession) {
      return sessionService.createSession(newSession)
        .then(function success(response) {
          var data = response.data;
          Sessions.sessions.push(data);
        }, function error(response) {
          $log.error('sessionService.createSession failed: ', response);
          throw response;
        });
    }

    function createRecurringGroup(newSession, recurringType) {
      return sessionService.createRecurringSessions(newSession, recurringType)
        .then(function success(response) {
          var data = response.data;
          angular.forEach(data, function (session) {
            Sessions.sessions.push(session);
          });
        }, function error(response) {
          $log.error('sessionService.createRecurringSessions failed: ', response);
          throw response;
        });
    }

    function createFromScheduleTemplate(scheduleTemplate, startDate) {
      return sessionService.createFromScheduleTemplate(scheduleTemplate, startDate)
        .then(function success(response) {
          var createdSessions = response.data;
          angular.forEach(createdSessions, function (session) {
            Sessions.sessions.push(session);
          });
        }, function error(response) {
          $log.error(response);
        });
    }

    function update(updatedSession) {
      return sessionService.updateSession(updatedSession)
        .then(function success() {
          var index = findSessionIndexWithId(updatedSession.id);
          Sessions.sessions[index] = updatedSession;
        }, function error(response) {
          $log.error('sessionService.updateSession failed: ', response);
          throw response;
        });
    }

    function remove(session) {
      return sessionService.deleteSession(session.id)
        .then(function success() {
          var index = findSessionIndexWithId(session.id);
          Sessions.sessions.splice(index, 1);
        }, function error(response) {
          $log.error('sessionService.deleteSession failed: ', response);
          throw response;
        });
    }

    function removeRecurringGroup(session) {
      return sessionService.removeRecurringSessions(session)
        .then(function success() {
          getAll();
        }, function error(response) {
          $log.error('sessionService.deleteSession failed with: ', response);
          throw response;
        });
    }
  }]);