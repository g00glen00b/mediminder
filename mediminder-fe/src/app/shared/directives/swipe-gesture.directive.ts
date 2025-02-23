import {computed, DestroyRef, Directive, ElementRef, inject, input, output} from '@angular/core';
import {combineLatestWith, filter, fromEvent, map, switchMap, zip} from "rxjs";
import {SwipeGesture} from "../models/swipe-gesture";
import {takeUntilDestroyed, toObservable} from '@angular/core/rxjs-interop';

@Directive({
    selector: '[swipeGesture]',
    standalone: true
})
export class SwipeGestureDirective {
  private readonly destroyRef = inject(DestroyRef);
  private readonly el = inject(ElementRef);

  swipeTarget = input<'element' | 'document'>('element');
  minimumSwipeDistance = input(100);
  swipeLeft = output<SwipeGesture>();
  swipeRight = output<SwipeGesture>();
  swipeUp = output<SwipeGesture>();
  swipeDown = output<SwipeGesture>();

  element = computed<Element>(() => this.swipeTarget() === 'document' ? document : this.el.nativeElement);


  constructor() {
    if (this.isTouchSupported()) {
      toObservable(this.element)
        .pipe(
          takeUntilDestroyed(this.destroyRef),
          switchMap(element => zip(
            fromEvent<TouchEvent>(element, 'touchstart'),
            fromEvent<TouchEvent>(element, 'touchend'))),
          combineLatestWith(toObservable(this.minimumSwipeDistance)),
          map(input => this.mapToGesture(input)),
          filter(gesture => gesture != null))
        .subscribe(gesture => this.emit(gesture));
    }
  }

  private mapToGesture(input: [[TouchEvent, TouchEvent], number]): SwipeGesture | null {
    const [[start, end], minimumDistance] = input;
    const {screenX: startX, screenY: startY} = start.changedTouches[0];
    const {screenX: endX, screenY: endY} = end.changedTouches[0];
    const diffX = endX - startX;
    const diffY = endY - startY;
    const absoluteDiffX = Math.abs(diffX);
    const absoluteDiffY = Math.abs(diffY);
    if (absoluteDiffX > minimumDistance && absoluteDiffX > absoluteDiffY) {
      const direction = diffX > 0 ? 'right' : 'left';
      return {start, end, direction};
    } else if (absoluteDiffY > minimumDistance && absoluteDiffY > absoluteDiffX) {
      const direction = diffY > 0 ? 'down' : 'up';
      return {start, end, direction};
    } else {
      return null;
    }
  }

  private isTouchSupported(): boolean {
    return matchMedia('(pointer: coarse)').matches;
  }

  private emit(gesture: SwipeGesture) {
    switch (gesture.direction) {
      case 'left':
        this.swipeLeft.emit(gesture);
        break;
      case 'right':
        this.swipeRight.emit(gesture);
        break;
      case 'up':
        this.swipeUp.emit(gesture);
        break;
      case 'down':
        this.swipeDown.emit(gesture);
        break;
    }
  }
}
