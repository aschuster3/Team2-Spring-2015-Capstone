angular.module('mwl.calendar')
  .factory('Sessions', ['sessionService', '$log', function(sessionService, $log) {
    var Sessions = {
      sessions: [],
      create: create,
      createRecurrence: createRecurrence,
      update: update,
      delete: remove,
      deleteRecurrence: removeRecurrence
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
      sessionService.getSessions()
        .success(function (data) {
          Sessions.sessions.splice(0, Sessions.sessions.length);
          angular.forEach(data, function (session) {
            Sessions.sessions.push(session);
          });
        })
        .error(function (data, status) {
          $log.error('sessionService.getSessions failed with code ' + status);
        });
    }

    function create(newSession) {
      sessionService.createSession(newSession)
        .success(function (data) {
          Sessions.sessions.push(data);
        })
        .error(function (data, status) {
          $log.error('sessionService.createSession failed with code ' + status);
        });
    }

    function createRecurrence(newSession, recurrenceType) {
      sessionService.createSessionRecurrence(newSession, recurrenceType)
        .success(function (data) {
          angular.forEach(data, function (session) {
            Sessions.sessions.push(session);
          });
        })
        .error(function (data, status) {
          $log.error('sessionService.createSessionRecurrence failed with code ' + status);
        })
    }

    function update(updatedSession) {
      sessionService.updateSession(updatedSession)
        .success(function () {
          var index = findSessionIndexWithId(updatedSession.id);
          Sessions.sessions[index] = updatedSession;
        })
        .error(function (data, status) {
          $log.error('sessionService.updateSession failed with code ' + status);
        })
    }

    function remove(session) {
      sessionService.deleteSession(session.id)
        .success(function () {
          var index = findSessionIndexWithId(session.id);
          Sessions.sessions.splice(index, 1);
        })
        .error(function (data, status) {
          $log.error('sessionService.deleteSession failed with code ' + status);
        })
    }

    function removeRecurrence(session) {
      sessionService.removeSessionRecurrence(session)
        .success(function () {
          getAll();
        })
        .error(function (data, status) {
          $log.error('sessionService.deleteSession failed with code ' + status);
        })
    }
  }]);