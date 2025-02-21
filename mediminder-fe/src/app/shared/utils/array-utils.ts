export function sum(arr: number[]): number {
  return arr.reduce((sum, value) => sum + value, 0);
}

export function average(arr: number[]): number {
  return sum(arr) / arr.length;
}
