import {Directive, ElementRef, EventEmitter, inject, Input, OnInit, Output} from '@angular/core';
import {fromEvent, map, Observable, zip} from "rxjs";
import {SwipeGesture} from "../models/swipe-gesture";

@Directive({
    selector: '[swipeGesture]',
    standalone: true
})
export class SwipeGestureDirective implements OnInit {
  @Input()
  swipeTarget: 'element' | 'document' = 'element';
  @Input()
  minimumSwipeDistance: number = 100;
  @Output()
  swipeLeft: EventEmitter<SwipeGesture> = new EventEmitter<SwipeGesture>();
  @Output()
  swipeRight: EventEmitter<SwipeGesture> = new EventEmitter<SwipeGesture>();
  @Output()
  swipeUp: EventEmitter<SwipeGesture> = new EventEmitter<SwipeGesture>();
  @Output()
  swipeDown: EventEmitter<SwipeGesture> = new EventEmitter<SwipeGesture>();
  private readonly el = inject(ElementRef);

  ngOnInit(): void {
    if (this.isTouchSupported()) {
      const element = this.swipeTarget === 'document' ? document : this.el.nativeElement;
      const touchStart: Observable<TouchEvent> = fromEvent(element, 'touchstart');
      const touchEnd: Observable<TouchEvent> = fromEvent(element, 'touchend');
      zip(touchStart, touchEnd)
        .pipe(map(([start, end]) => ({start, end})))
        .subscribe(gesture => this.emit(gesture));
    }
  }

  private isTouchSupported(): boolean {
    return matchMedia('(pointer: coarse)').matches;
  }

  private emit(gesture: SwipeGesture): void {
    const {screenX: startX, screenY: startY} = gesture.start.changedTouches[0];
    const {screenX: endX, screenY: endY} = gesture.end.changedTouches[0];
    const diffX = endX - startX;
    const diffY = endY - startY;
    const absoluteDiffX = Math.abs(diffX);
    const absoluteDiffY = Math.abs(diffY);
    if (absoluteDiffX > this.minimumSwipeDistance && absoluteDiffX > absoluteDiffY) {
      if (diffX > 0) this.swipeRight.emit(gesture);
      else this.swipeLeft.emit(gesture);
    } else if (absoluteDiffY > this.minimumSwipeDistance && absoluteDiffY > absoluteDiffX) {
      if (diffY > 0) this.swipeDown.emit(gesture);
      else this.swipeUp.emit(gesture);
    }
  }

}
