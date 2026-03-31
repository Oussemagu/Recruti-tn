import { HttpInterceptorFn ,HttpRequest, HttpHandlerFn} from '@angular/common/http';

// Intercepteur fonctionnel Angular 17+
// Ajoute automatiquement le token JWT dans le header
// de CHAQUE requête HTTP sortante
export const jwtInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {

  // Récupère le token depuis localStorage
  const stored = localStorage.getItem('recruiti_user');
  const token = stored ? JSON.parse(stored).token : null;

  // Si token présent → clone la requête avec le header Authorization
  if (token) {
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(cloned);
  }

  // Sinon → laisse passer la requête sans modification
  return next(req);
};
