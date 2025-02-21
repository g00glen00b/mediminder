export type Comparator<Type> = (object1: Type, object2: Type) => number;

export function compareBy<Type>(comparators: Array<Comparator<Type>>): Comparator<Type> {
  return (object1, object2) => {
    return comparators
      .map(comparator => comparator(object1, object2))
      .find(result => result !== 0) || 0;
  };
}

export function compareByField<Type>(extractor: (parameter: Type) => any): Comparator<Type> {
  return (object1, object2) => {
    const value1 = extractor(object1);
    const value2 = extractor(object2);
    if (value1 === value2) return 0;
    else if (value1 == null) return 1;
    else if (value2 == null) return -1;
    else if (value1 < value2) return -1;
    else return 1;
  };
}

export function reversed<Type>(comparator: Comparator<Type>): Comparator<Type> {
  return (object1, object2) => {
    const result = comparator(object1, object2);
    if (result === 1) return -1;
    else if (result === -1) return 1;
    else return 0;
  };
}
