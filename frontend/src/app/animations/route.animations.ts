import { animate, query, style, transition, trigger } from '@angular/animations';

export const routeFadeAnimation = trigger('routeFade', [
  transition('* <=> *', [
    query(
      ':enter',
      [style({ opacity: 0 })],
      { optional: true },
    ),
    query(
      ':leave',
      [animate('180ms ease-out', style({ opacity: 0 }))],
      { optional: true },
    ),
    query(
      ':enter',
      [animate('220ms ease-in', style({ opacity: 1 }))],
      { optional: true },
    ),
  ]),
]);
