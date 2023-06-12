import {Directive, ElementRef, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {fromEvent, map, Observable, zip} from "rxjs";
import {SwipeGesture} from "../models/swipe-gesture";

@Directive({
  selector: '[swipeGesture]'
})
export class SwipeGestureDirective implements OnInit {
  @Input()
  swipeGesture: 'element' | 'document' | '' = 'element';
  @Output()
  swipeLeft: EventEmitter<SwipeGesture> = new EventEmitter<SwipeGesture>();
  @Output()
  swipeRight: EventEmitter<SwipeGesture> = new EventEmitter<SwipeGesture>();
  @Output()
  swipeUp: EventEmitter<SwipeGesture> = new EventEmitter<SwipeGesture>();
  @Output()
  swipeDown: EventEmitter<SwipeGesture> = new EventEmitter<SwipeGesture>();

  constructor(private el: ElementRef) { }

  ngOnInit(): void {
    if (this.isTouchSupported()) {
      const element = this.swipeGesture === 'document' ? document : this.el.nativeElement;
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
    if (Math.abs(diffX) > Math.abs(diffY)) {
      if (diffX > 0) this.swipeRight.emit(gesture);
      else this.swipeLeft.emit(gesture);
    } else {
      if (diffY > 0) this.swipeDown.emit(gesture);
      else this.swipeUp.emit(gesture);
    }
  }

}
