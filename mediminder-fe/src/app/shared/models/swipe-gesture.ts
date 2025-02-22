export interface SwipeGesture {
  start: TouchEvent;
  end: TouchEvent;
  direction: 'left' | 'right' | 'up' | 'down';
}
