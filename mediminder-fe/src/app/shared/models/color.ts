export type Color = 'RED' | 'ORANGE' | 'YELLOW' | 'GREEN' | 'BLUE' | 'INDIGO' | 'VIOLET' | 'BLACK' | 'WHITE' | 'GRAY';

export interface ColorOption {
  color: Color;
  name: string;
}

export function colorOptions(): ColorOption[] {
  return [
    {color: 'BLACK', name: 'Black'},
    {color: 'GRAY', name: 'Gray'},
    {color: 'WHITE', name: 'White'},
    {color: 'RED', name: 'Red'},
    {color: 'ORANGE', name: 'Orange'},
    {color: 'YELLOW', name: 'Yellow'},
    {color: 'GREEN', name: 'Green'},
    {color: 'BLUE', name: 'Blue'},
    {color: 'INDIGO', name: 'Indigo'},
    {color: 'VIOLET', name: 'Violet'},
  ];
}
