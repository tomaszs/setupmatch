import { animate, animateChild, query, stagger, style, transition, trigger } from '@angular/animations';

export const fadeInAnimation = trigger('fadeIn', [
  transition(':enter', [
    style({ opacity: 0 }),
    animate('240ms ease-out', style({ opacity: 1 })),
  ]),
]);

export const fadeCardAnimation = trigger('fadeCard', [
  transition(':enter', [
    style({ opacity: 0, transform: 'translateY(8px)' }),
    animate('280ms ease-out', style({ opacity: 1, transform: 'translateY(0)' })),
  ]),
]);

export const slotListAnimation = trigger('slotList', [
  transition('* => *', [
    query('@slotItem', animateChild(), { optional: true }),
  ]),
]);

export const slotItemAnimation = trigger('slotItem', [
  transition(':enter', [
    style({ opacity: 0, transform: 'translateY(8px)' }),
    animate('220ms ease-out', style({ opacity: 1, transform: 'translateY(0)' })),
  ]),
  transition(':leave', [
    animate('160ms ease-in', style({ opacity: 0, transform: 'translateY(-6px)' })),
  ]),
]);

export const staggerFadeAnimation = trigger('staggerFade', [
  transition('* => *', [
    query(
      ':enter',
      [
        style({ opacity: 0, transform: 'translateY(6px)' }),
        stagger(60, [
          animate('220ms ease-out', style({ opacity: 1, transform: 'translateY(0)' })),
        ]),
      ],
      { optional: true },
    ),
  ]),
]);
